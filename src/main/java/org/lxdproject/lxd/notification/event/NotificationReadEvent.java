package org.lxdproject.lxd.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationReadEvent {
    private final Long memberId;
    private final Long notificationId;
}

