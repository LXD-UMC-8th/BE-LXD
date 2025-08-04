package org.lxdproject.lxd.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.dto.PageResponse;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.correctioncomment.repository.CorrectionCommentRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        NotificationMessageContext publishEventDTO = NotificationMessageContext.of(notification, senderUsername, diaryTitle);

        // Redis에 publish
        notificationPublisher.publish(publishEventDTO);
    }

    @Transactional(readOnly = true)
    protected String getDiaryTitleIfExists(Notification notification) {
        Long targetId = notification.getTargetId();

        return switch (notification.getNotificationType()) {
            case COMMENT_ADDED ->
                    diaryCommentRepository.findDiaryTitleByCommentId(targetId).orElse(null);
            case CORRECTION_ADDED ->
                    correctionRepository.findDiaryTitleByCorrectionId(targetId).orElse(null);
            case CORRECTION_REPLIED ->
                    correctionCommentRepository.findDiaryTitleByCorrectionCommentId(targetId).orElse(null);
            default -> null;
        };
    }

    public PageResponse<NotificationResponseDTO> getNotifications(Boolean isRead, int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Notification> notificationPage = notificationRepository.findPageByMemberId(memberId, isRead, pageable);

        List<NotificationResponseDTO> notificationS = notificationPage.stream()
                .map(notification -> {
                    Locale locale = member.getNativeLanguage().toLocale();
                    String senderUsername = notification.getSender().getUsername();
                    String diaryTitle = getDiaryTitleIfExists(notification);

                    NotificationMessageContext context = NotificationMessageContext.of(
                            notification,
                            senderUsername,
                            diaryTitle
                    );

                    List<MessagePart> parts = messageResolverManager.resolve(context, locale);

                    return NotificationResponseDTO.builder()
                            .id(notification.getId())
                            .buttonField(notification.getNotificationType() == NotificationType.FRIEND_REQUEST)
                            .profileImg(notification.getSender().getProfileImg())
                            .messageParts(parts)
                            .redirectUrl(notification.getRedirectUrl())
                            .isRead(notification.isRead())
                            .createdAt(DateFormatUtil.formatDate(notification.getCreatedAt()))
                            .build();
                })
                .toList();

        return new PageResponse<>(
                notificationPage.getTotalElements(),
                notificationS,
                page + 1,
                size,
                notificationPage.getTotalPages(),
                notificationPage.hasNext()
        );
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
    public PageResponse<NotificationResponseDTO>  markAllAsRead(int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<Notification> unreadList = notificationRepository.findUnreadWithSenderByReceiverId(memberId);

        for (Notification notification : unreadList) {
            notification.markAsRead();
        }

        sseEmitterService.sendAllReadUpdate(memberId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Notification> notificationPage = notificationRepository.findPageByMemberId(memberId, null, pageable);

        List<NotificationResponseDTO> responses = notificationPage.stream()
                .map(notification -> {
                    Locale locale = member.getNativeLanguage().toLocale();
                    String senderUsername = notification.getSender().getUsername();
                    String diaryTitle = getDiaryTitleIfExists(notification);

                    NotificationMessageContext context = NotificationMessageContext.of(
                            notification,
                            senderUsername,
                            diaryTitle
                    );

                    List<MessagePart> parts = messageResolverManager.resolve(context, locale);

                    return NotificationResponseDTO.builder()
                            .id(notification.getId())
                            .buttonField(notification.getNotificationType() == NotificationType.FRIEND_REQUEST)
                            .profileImg(notification.getSender().getProfileImg())
                            .messageParts(parts)
                            .redirectUrl(notification.getRedirectUrl())
                            .isRead(notification.isRead())
                            .createdAt(DateFormatUtil.formatDate(notification.getCreatedAt()))
                            .build();
                })
                .toList();

        return new PageResponse<>(
                notificationPage.getTotalElements(),
                responses,
                page + 1,
                size,
                notificationPage.getTotalPages(),
                notificationPage.hasNext()
        );
    }

}

