package org.lxdproject.lxd.notification.message;

import org.lxdproject.lxd.notification.dto.MessagePart;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class FriendAcceptedResolver implements MessageResolver {
    @Override
    public NotificationType getSupportedType() {
        return NotificationType.FRIEND_ACCEPTED;
    }

    @Override
    public List<MessagePart> resolveParts(MessageContext event, Locale locale) {
        String senderUsername = "@" + event.getSenderUsername();

        if (locale.getLanguage().equals("en")) {
            return List.of(
                    new MessagePart("text", "You are now friends with "),
                    new MessagePart("bold", senderUsername)
            );
        } else {
            return List.of(
                    new MessagePart("bold", senderUsername),
                    new MessagePart("text", "님과 친구가 되었습니다.")
            );
        }
    }
}
