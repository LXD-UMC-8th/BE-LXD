package org.lxdproject.lxd.notification.message;

import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class FriendNotificationMessageResolver implements NotificationMessageResolver {

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.FRIEND_REQUEST || type == NotificationType.FRIEND_ACCEPTED;
    }

    @Override
    public String resolveMessage(NotificationRequestDTO dto, Member sender, Locale locale) {
        LocalizedMessageTemplate template = switch (dto.getNotificationType()) {
            case FRIEND_REQUEST -> new LocalizedMessageTemplate(
                    "%s님이 친구를 요청했습니다.",
                    "You are now friends with %s");
            case FRIEND_ACCEPTED -> new LocalizedMessageTemplate(
                    "%s님과 친구가 되었습니다.",
                    "%s sent you a friend request.");
            default -> throw new NotificationHandler(ErrorStatus.TARGET_TYPE_UNSUPPORTED);
        };

        return template.format(sender.getNickname(), null, locale);
    }
}
