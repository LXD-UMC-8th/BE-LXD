package org.lxdproject.lxd.notification.message;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.notification.dto.MessagePart;
import org.lxdproject.lxd.notification.dto.NotificationMessageContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NotificationMessageResolverManager {

    private final List<NotificationMessageResolver> resolvers;

    public List<MessagePart> resolve(NotificationMessageContext publishEvent, Locale locale) {
        return resolvers.stream()
                .filter(resolver -> resolver.supports(publishEvent.getNotificationType()))
                .findFirst()
                .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_TYPE_NOT_SUPPORTED))
                .resolveParts(publishEvent, locale);
    }

}
