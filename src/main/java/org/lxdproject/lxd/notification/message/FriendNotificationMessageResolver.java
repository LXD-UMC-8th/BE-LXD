package org.lxdproject.lxd.notification.message;

import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.notification.dto.MessagePart;
import org.lxdproject.lxd.notification.dto.NotificationPublishEvent;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class FriendNotificationMessageResolver implements NotificationMessageResolver {

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.FRIEND_REQUEST || type == NotificationType.FRIEND_ACCEPTED;
    }

    @Override
    public List<MessagePart> resolveParts(NotificationPublishEvent event, Locale locale) {
        String senderUsername = "@" + event.getSenderUsername();
        NotificationType type = event.getNotificationType();

        if (locale.getLanguage().equals("en")) {
            return switch (type) {
                case FRIEND_REQUEST -> List.of(
                        new MessagePart("bold", senderUsername),
                        new MessagePart("text", " sent you a friend request.")
                );
                case FRIEND_ACCEPTED -> List.of(
                        new MessagePart("text", "You are now friends with "),
                        new MessagePart("bold", senderUsername)
                );
                default -> throw new NotificationHandler(ErrorStatus.TARGET_TYPE_UNSUPPORTED);
            };
        } else {
            return switch (type) {
                case FRIEND_REQUEST -> List.of(
                        new MessagePart("bold", senderUsername),
                        new MessagePart("text", "님이 친구를 요청했습니다.")
                );
                case FRIEND_ACCEPTED -> List.of(
                        new MessagePart("bold", senderUsername),
                        new MessagePart("text", "님과 친구가 되었습니다.")
                );
                default -> throw new NotificationHandler(ErrorStatus.TARGET_TYPE_UNSUPPORTED);
            };
        }
    }

}
