package org.lxdproject.lxd.notification.dto;

import lombok.*;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationPublishEvent {
    private Long notificationId;
    private Long receiverId;
    private Long senderId;
    private String senderUsername;
    private NotificationType notificationType;
    private TargetType targetType;
    private Long targetId;
    private String diaryTitle;

    public static NotificationPublishEvent of(Notification notification, String senderUsername, String diaryTitle) {
        return NotificationPublishEvent.builder()
                .notificationId(notification.getId())
                .receiverId(notification.getReceiver().getId())
                .senderId(notification.getSender().getId())
                .senderUsername(senderUsername)
                .diaryTitle(diaryTitle)
                .notificationType(notification.getNotificationType())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .build();
    }

}
