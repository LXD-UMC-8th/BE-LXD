package org.lxdproject.lxd.notification.message;

import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;

import java.util.Locale;

public interface NotificationMessageResolver {
    boolean supports(NotificationType type);
    String resolveMessage(NotificationRequestDTO dto, Member sender, Locale locale);
}
