package org.lxdproject.lxd.domain.notification.message;

import org.lxdproject.lxd.domain.notification.dto.MessagePart;
import org.lxdproject.lxd.domain.notification.entity.enums.NotificationType;

import java.util.List;
import java.util.Locale;

public interface MessageResolver {
    NotificationType getSupportedType();
    List<MessagePart> resolveParts(MessageContext event, Locale locale);
}
