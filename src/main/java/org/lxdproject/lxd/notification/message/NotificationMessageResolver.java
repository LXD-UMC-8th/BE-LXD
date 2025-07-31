package org.lxdproject.lxd.notification.message;

import org.lxdproject.lxd.notification.dto.MessagePart;
import org.lxdproject.lxd.notification.dto.NotificationMessageContext;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;

import java.util.List;
import java.util.Locale;

public interface NotificationMessageResolver {
    boolean supports(NotificationType type);
    List<MessagePart> resolveParts(NotificationMessageContext event, Locale locale);
}
