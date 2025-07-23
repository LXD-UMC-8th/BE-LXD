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
public class NotificationMessageDTO {
    private Long receiverId;
    private Long senderId;
    private NotificationType notificationType;
    private String title;
    private String body;
    private TargetType targetType;
    private Long targetId;

    public static NotificationMessageDTO from(Notification notification) {
        return NotificationMessageDTO.builder()
                .receiverId(notification.getReceiver().getId())
                .senderId(notification.getSender() != null ? notification.getSender().getId() : null)
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .build();
    }

}
