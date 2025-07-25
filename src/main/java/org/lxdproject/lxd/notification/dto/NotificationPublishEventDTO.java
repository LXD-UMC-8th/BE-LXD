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
public class NotificationPublishEventDTO {
    private Long notificationId;
    private Long receiverId;
    private Long senderId;
    private NotificationType notificationType;
    private TargetType targetType;
    private Long targetId;

    public static NotificationPublishEventDTO from(Notification notification) {
        return NotificationPublishEventDTO.builder()
                .notificationId(notification.getId())
                .receiverId(notification.getReceiver().getId())
                .senderId(notification.getSender().getId())
                .notificationType(notification.getNotificationType())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .build();
    }

}
