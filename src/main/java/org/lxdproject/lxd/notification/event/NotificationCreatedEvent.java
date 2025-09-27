package org.lxdproject.lxd.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.notification.entity.Notification;

@Getter
@RequiredArgsConstructor
public class NotificationCreatedEvent {
    private final Notification notification;
}
