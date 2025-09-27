package org.lxdproject.lxd.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;

@Getter
@RequiredArgsConstructor
public class NotificationDeletedEvent {
    private final Long notificationId;
    private final Long receiverId;
    private final NotificationType notificationType;
    private final TargetType targetType;
    private final Long targetId;
}

