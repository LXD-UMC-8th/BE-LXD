package org.lxdproject.lxd.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.correctioncomment.repository.CorrectionCommentRepository;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.notification.message.MessageContext;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.enums.EventType;
import org.lxdproject.lxd.notification.publisher.NotificationPublisher;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    private final DiaryCommentRepository diaryCommentRepository;
    private final CorrectionRepository correctionRepository;
    private final CorrectionCommentRepository correctionCommentRepository;

    // 생성 이벤트 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(NotificationCreatedEvent event) {
        MessageContext message = MessageContext.builder()
                .eventType(EventType.CREATED)
                .notificationId(event.getNotificationId())
                .receiverId(event.getReceiverId())
                .senderId(event.getSenderId())
                .senderUsername(event.getSenderUsername())
                .notificationType(event.getNotificationType())
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .redirectUrl(event.getRedirectUrl())
                .diaryTitle(event.getDiaryTitle())
                .build();

        notificationPublisher.publishAfterCommit(message);
    }


    // 삭제 이벤트 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleted(NotificationDeletedEvent event) {
        MessageContext message = MessageContext.builder()
                .eventType(EventType.DELETED)
                .receiverId(event.getReceiverId())
                .notificationId(event.getNotificationId())
                .notificationType(event.getNotificationType())
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .build();

        notificationPublisher.publishAfterCommit(message);
    }

    // 단일 읽음 이벤트 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRead(NotificationReadEvent event) {
        MessageContext message = MessageContext.builder()
                .eventType(EventType.READ)
                .receiverId(event.getMemberId())
                .notificationId(event.getNotificationId())
                .build();

        notificationPublisher.publishAfterCommit(message);
    }

    // 전체 읽음 이벤트 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAllRead(NotificationAllReadEvent event) {
        MessageContext message = MessageContext.builder()
                .eventType(EventType.ALL_READ)
                .receiverId(event.getMemberId())
                .build();

        notificationPublisher.publishAfterCommit(message);
    }
}

