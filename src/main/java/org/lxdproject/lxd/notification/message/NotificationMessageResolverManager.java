package org.lxdproject.lxd.notification.message;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NotificationMessageResolverManager {

    private final List<NotificationMessageResolver> resolvers;

    public String resolve(NotificationRequestDTO dto, Member sender, Locale locale) {
        return resolvers.stream()
                .filter(r -> r.supports(dto.getNotificationType()))
                .findFirst()
                .orElseThrow(() -> new NotificationHandler(ErrorStatus.TARGET_TYPE_UNSUPPORTED))
                .resolveMessage(dto, sender, locale);
    }
}
