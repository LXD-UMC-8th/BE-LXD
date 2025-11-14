package org.lxdproject.lxd.domain.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.domain.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.domain.notification.entity.enums.TargetType;

@Getter
@AllArgsConstructor
@Builder
public class NotificationCreatedEvent {
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
