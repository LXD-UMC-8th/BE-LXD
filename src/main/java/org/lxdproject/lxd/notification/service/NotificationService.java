package org.lxdproject.lxd.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.dto.PageDTO;
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
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.lxdproject.lxd.notification.event.NotificationAllReadEvent;
import org.lxdproject.lxd.notification.event.NotificationCreatedEvent;
import org.lxdproject.lxd.notification.event.NotificationDeletedEvent;
import org.lxdproject.lxd.notification.event.NotificationReadEvent;
import org.lxdproject.lxd.notification.message.MessageResolverManager;
import org.lxdproject.lxd.notification.message.MessageContext;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;
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
    private final MemberRepository memberRepository;
    private final CorrectionCommentRepository correctionCommentRepository;
    private final CorrectionRepository correctionRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final MessageResolverManager messageResolverManager;
    private final ApplicationEventPublisher eventPublisher;

    public Long createNotification(NotificationRequestDTO dto) {

        Member receiver = memberRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member sender = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .receiver(receiver)
                        .sender(sender)
                        .notificationType(dto.getNotificationType())
                        .targetType(dto.getTargetType())
                        .targetId(dto.getTargetId())
                        .redirectUrl(dto.getRedirectUrl())
                        .build()
        );

        return notification.getId();
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

    public PageDTO<NotificationResponseDTO> getNotifications(Boolean isRead, int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage = notificationRepository.findPageByMemberId(memberId, isRead, pageable);

        List<NotificationResponseDTO> notificationS = notificationPage.stream()
                .map(notification -> {
                    Locale locale = member.getSystemLanguage().toLocale();
                    String senderUsername = notification.getSender().getUsername();
                    String diaryTitle = getDiaryTitleIfExists(notification);

                    MessageContext context = MessageContext.builder()
                            .notificationId(notification.getId())
                            .receiverId(notification.getReceiver().getId())
                            .senderId(notification.getSender().getId())
                            .senderUsername(senderUsername)
                            .notificationType(notification.getNotificationType())
                            .targetType(notification.getTargetType())
                            .targetId(notification.getTargetId())
                            .redirectUrl(notification.getRedirectUrl())
                            .diaryTitle(diaryTitle)
                            .build();

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

        return new PageDTO<>(
                notificationPage.getTotalElements(),
                notificationS,
                page + 1,
                size,
                notificationPage.hasNext()
        );
    }

    @Transactional
    public ReadRedirectResponseDTO markAsRead(Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getId().equals(memberId)) {
            throw new AuthHandler(ErrorStatus.NOT_RESOURCE_OWNER);
        }

        if (!notification.isRead()) {
            notification.markAsRead();
            notificationRepository.flush();

            eventPublisher.publishEvent(
                    new NotificationReadEvent(memberId, notification.getId())
            );
        }

        return ReadRedirectResponseDTO.builder()
                .notificationId(notification.getId())
                .redirectUrl(notification.getRedirectUrl())
                .isRead(true)
                .build();
    }

    @Transactional
    public PageDTO<NotificationResponseDTO> markAllAsRead(int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<Notification> unreadList = notificationRepository.findUnreadWithSenderByReceiverId(memberId);

        for (Notification notification : unreadList) {
            notification.markAsRead();
        }

        notificationRepository.flush();

        eventPublisher.publishEvent(
                new NotificationAllReadEvent(memberId)
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage = notificationRepository.findPageByMemberId(memberId, null, pageable);

        List<NotificationResponseDTO> responses = notificationPage.stream()
                .map(notification -> {
                    Locale locale = member.getSystemLanguage().toLocale();
                    String senderUsername = notification.getSender().getUsername();
                    String diaryTitle = getDiaryTitleIfExists(notification);

                    MessageContext context = MessageContext.builder()
                            .notificationId(notification.getId())
                            .receiverId(notification.getReceiver().getId())
                            .senderId(notification.getSender().getId())
                            .senderUsername(senderUsername)
                            .notificationType(notification.getNotificationType())
                            .targetType(notification.getTargetType())
                            .targetId(notification.getTargetId())
                            .redirectUrl(notification.getRedirectUrl())
                            .diaryTitle(diaryTitle)
                            .build();

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

        return new PageDTO<>(
                notificationPage.getTotalElements(),
                responses,
                page + 1,
                size,
                notificationPage.hasNext()
        );
    }

    @Transactional
    public void createAndPublish(NotificationRequestDTO dto) {
        Long notificationId = createNotification(dto);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND));

        eventPublisher.publishEvent(
                NotificationCreatedEvent.builder()
                    .notificationId(notificationId)
                    .receiverId(notification.getReceiver().getId())
                    .senderId(notification.getSender().getId())
                    .senderUsername(notification.getSender().getUsername())
                    .notificationType(notification.getNotificationType())
                    .targetType(notification.getTargetType())
                    .targetId(notification.getTargetId())
                    .redirectUrl(notification.getRedirectUrl())
                    .diaryTitle(getDiaryTitleIfExists(notification))
                    .build()
        );
    }

    @Transactional
    public void deleteAndPublish(Long receiverId, Long senderId, NotificationType type, TargetType targetType, Long targetId) {

        // 삭제할 친구 요청 알림 ID 조회
        Long notificationId = notificationRepository.findFriendRequestNotificationId(receiverId, senderId);
        if (notificationId == null) {
            throw new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND);
        }

        // 친구 요청 알림 삭제
        notificationRepository.deleteFriendRequestNotification(receiverId, senderId);

        // 친구 요청 알림 삭제
        eventPublisher.publishEvent(
                new NotificationDeletedEvent(notificationId, receiverId, type, targetType, targetId)
        );
    }
}

