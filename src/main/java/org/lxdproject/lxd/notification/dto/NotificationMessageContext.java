package org.lxdproject.lxd.notification.dto;

import lombok.*;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.enums.EventType;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationMessageContext {
    private EventType eventType;
    private Long notificationId;
    private Long receiverId;
    private Long senderId;
    private String senderUsername;
    private NotificationType notificationType;
    private TargetType targetType;
    private Long targetId;
    private String diaryTitle;
    private String redirectUrl;
}
