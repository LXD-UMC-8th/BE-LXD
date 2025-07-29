package org.lxdproject.lxd.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationReadUpdateDTO {
    private Long notificationId;
    private boolean isRead;
}

