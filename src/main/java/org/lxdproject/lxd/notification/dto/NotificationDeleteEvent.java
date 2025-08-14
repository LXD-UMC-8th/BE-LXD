package org.lxdproject.lxd.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;

@Getter
@AllArgsConstructor
public class NotificationDeleteEvent {
    private final NotificationType type; // FRIEND_REQUEST
    private final TargetType targetType; // MEMBER
    private final Long targetId; // 친구 요청자
}
