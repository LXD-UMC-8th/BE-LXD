package org.lxdproject.lxd.notification.dto;

import lombok.Getter;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;

@Getter
public class NotificationRequestDTO {
    private TargetType targetType;
    private Long targetId;
    private NotificationType notificationType;
    private Long receiverId;
    private String redirectUrl;
}