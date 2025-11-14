package org.lxdproject.lxd.domain.notification.message;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.global.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.domain.notification.dto.MessagePart;
import org.lxdproject.lxd.domain.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.domain.notification.entity.enums.TargetType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DiaryCommentResolver implements MessageResolver {
    @Override
    public NotificationType getSupportedType() {
        return NotificationType.COMMENT_ADDED;
    }

    @Override
    public List<MessagePart> resolveParts(MessageContext event, Locale locale) {
        if (event.getTargetType() != TargetType.DIARY_COMMENT) {
            throw new NotificationHandler(ErrorStatus.TARGET_TYPE_MISMATCH);
        }

        String diaryTitle = event.getDiaryTitle();
        String senderUsername = "@" + event.getSenderUsername();

        if (locale.getLanguage().equals("en")) {
            return List.of(
                    new MessagePart("bold", senderUsername),
                    new MessagePart("text", " commented on the "),
                    new MessagePart("bold", diaryTitle),
                    new MessagePart("text", " diary entry.")
            );
        } else {
            return List.of(
                    new MessagePart("bold", senderUsername),
                    new MessagePart("text", "님이 "),
                    new MessagePart("bold", diaryTitle),
                    new MessagePart("text", " 일기에 댓글을 작성했습니다.")
            );
        }
    }

}
