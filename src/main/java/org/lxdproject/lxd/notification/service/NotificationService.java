package org.lxdproject.lxd.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.dto.CursorPageResponse;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.correction.util.DateFormatUtil;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.correctioncomment.repository.CorrectionCommentRepository;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.*;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.message.NotificationMessageResolverManager;
import org.lxdproject.lxd.notification.publisher.NotificationPublisher;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;
    private final MemberRepository memberRepository;
    private final NotificationMessageResolverManager messageResolverManager;
    private final SseEmitterService sseEmitterService;
    private final CorrectionCommentRepository correctionCommentRepository;
    private final CorrectionRepository correctionRepository;
    private final DiaryCommentRepository diaryCommentRepository;

    public void saveAndPublishNotification(NotificationRequestDTO dto) {

        Member receiver = memberRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member sender = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // Notification 저장
        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .notificationType(dto.getNotificationType())
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .redirectUrl(dto.getRedirectUrl())
                .build();

        notificationRepository.save(notification);

        String senderUsername = sender.getUsername();
        String diaryTitle = getDiaryTitleIfExists(notification);

        // Notification을 기반으로 Redis에 발행되는 메시지 생성
        NotificationPublishEvent publishEventDTO = NotificationPublishEvent.of(notification, senderUsername, diaryTitle);

        // Redis에 publish
        notificationPublisher.publish(publishEventDTO);
    }

    @Transactional(readOnly = true)
    protected String getDiaryTitleIfExists(Notification notification) {

        // Todo. Projection으로 최적화 매우 필요
        NotificationType type = notification.getNotificationType();

        if (type == NotificationType.COMMENT_ADDED) {
            DiaryComment comment = diaryCommentRepository.findById(notification.getTargetId())
                    .orElse(null);
            if (comment != null && comment.getDiary() != null) {
                return comment.getDiary().getTitle();
            }
        }

        if (type == NotificationType.CORRECTION_ADDED) {
            Correction correction = correctionRepository.findById(notification.getTargetId())
                    .orElse(null);
            if (correction != null && correction.getDiary() != null) {
                return correction.getDiary().getTitle();
            }
        }

        if (type == NotificationType.CORRECTION_REPLIED) {
            CorrectionComment correctionComment = correctionCommentRepository.findById(notification.getTargetId())
                    .orElse(null);
            if (correctionComment != null) {
                return correctionComment.getCorrection().getDiary().getTitle();
            }
        }

        return null; // 나머지는 title이 필요 없는 경우
    }

    public CursorPageResponse<NotificationResponseDTO> getNotifications(Boolean isRead, Long lastId, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<Notification> notifications = notificationRepository.findNotificationsWithCursor(memberId, isRead, lastId, size);

        List<NotificationResponseDTO> content = notifications.stream()
                .map(notification -> {
                    Member sender = notification.getSender();
                    Locale locale = member.getNativeLanguage().toLocale();

                    List<MessagePart> parts = messageResolverManager.resolve(notification, locale);

                    return NotificationResponseDTO.builder()
                            .id(notification.getId())
                            .buttonField(notification.getNotificationType() == NotificationType.FRIEND_REQUEST)
                            .profileImg(sender.getProfileImg())
                            .messageParts(parts)
                            .redirectUrl(notification.getRedirectUrl())
                            .isRead(notification.isRead())
                            .createdAt(DateFormatUtil.formatDate(notification.getCreatedAt()))
                            .build();
                })
                .toList();

        boolean hasNext = content.size() == size;
        Long nextCursor = hasNext && !content.isEmpty() ? content.get(content.size() - 1).getId() : null;

        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    @Transactional
    public ReadRedirectResponseDTO markAsReadAndSendSse(Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getId().equals(memberId)) {
            throw new AuthHandler(ErrorStatus.NOT_RESOURCE_OWNER);
        }

        if (!notification.isRead()) {
            notification.markAsRead();
            sseEmitterService.sendNotificationReadUpdate(notification);
        }

        return ReadRedirectResponseDTO.builder()
                .notificationId(notification.getId())
                .redirectUrl(notification.getRedirectUrl())
                .isRead(true)
                .build();
    }

    @Transactional
    public String markAllAsRead() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        List<Notification> unreadList = notificationRepository.findUnreadWithSenderByReceiverId(memberId);

        for (Notification notification : unreadList) {
            notification.markAsRead();
        }

        sseEmitterService.sendAllReadUpdate(memberId);

        return unreadList.size() + "개의 알림이 읽음 처리 되었습니다.";
    }

}

