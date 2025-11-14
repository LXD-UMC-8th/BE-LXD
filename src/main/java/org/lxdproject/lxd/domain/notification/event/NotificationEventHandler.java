package org.lxdproject.lxd.domain.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.domain.notification.message.MessageContext;
import org.lxdproject.lxd.domain.notification.entity.enums.EventType;
import org.lxdproject.lxd.domain.notification.publisher.NotificationPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationPublisher notificationPublisher;

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

