package org.lxdproject.lxd.notification.message;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.notification.dto.MessagePart;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageResolverManager {

    private final Map<NotificationType, MessageResolver> resolverMap;

    @Autowired
    public MessageResolverManager(List<MessageResolver> resolvers) {
        this.resolverMap = resolvers.stream()
                .collect(Collectors.toMap(MessageResolver::getSupportedType, r -> r));
    }

    public List<MessagePart> resolve(MessageContext publishEvent, Locale locale) {
        MessageResolver resolver = resolverMap.get(publishEvent.getNotificationType());
        if (resolver == null) {
            throw new NotificationHandler(ErrorStatus.NOTIFICATION_TYPE_NOT_SUPPORTED);
        }
        return resolver.resolveParts(publishEvent, locale);
    }

}
