package org.lxdproject.lxd.domain.notification.message;

import org.lxdproject.lxd.domain.notification.dto.MessagePart;
import org.lxdproject.lxd.domain.notification.entity.enums.NotificationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class FriendRequestResolver implements MessageResolver {
    @Override
    public NotificationType getSupportedType() {
        return NotificationType.FRIEND_REQUEST;
    }

    @Override
    public List<MessagePart> resolveParts(MessageContext event, Locale locale) {
        String senderUsername = "@" + event.getSenderUsername();

        if (locale.getLanguage().equals("en")) {
            return List.of(
                    new MessagePart("bold", senderUsername),
                    new MessagePart("text", " sent you a friend request.")
            );
        } else {
            return List.of(
                    new MessagePart("bold", senderUsername),
                    new MessagePart("text", "님이 친구를 요청했습니다.")
            );
        }
    }
}
