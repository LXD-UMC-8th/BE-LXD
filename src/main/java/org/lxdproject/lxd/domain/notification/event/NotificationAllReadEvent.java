package org.lxdproject.lxd.domain.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationAllReadEvent {
    private final Long memberId;
}
