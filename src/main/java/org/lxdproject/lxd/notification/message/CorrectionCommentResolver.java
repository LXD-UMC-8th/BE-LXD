package org.lxdproject.lxd.notification.message;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.notification.dto.MessagePart;
import org.lxdproject.lxd.notification.dto.NotificationMessageContext;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CorrectionCommentResolver implements MessageResolver {
    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.CORRECTION_REPLIED;
    }

    @Override
    public List<MessagePart> resolveParts(NotificationMessageContext event, Locale locale) {
        if (event.getTargetType() != TargetType.CORRECTION_COMMENT) {
            throw new NotificationHandler(ErrorStatus.TARGET_TYPE_MISMATCH);
        }

        String diaryTitle = event.getDiaryTitle();
        String senderUsername = "@" + event.getSenderUsername();

        if (locale.getLanguage().equals("en")) {
            return List.of(
                    new MessagePart("bold", senderUsername),
                    new MessagePart("text", " replied to the correction on the "),
                    new MessagePart("bold", diaryTitle),
                    new MessagePart("text", " diary entry.")
            );
        } else {
            return List.of(
                    new MessagePart("bold", senderUsername),
                    new MessagePart("text", "님이 "),
                    new MessagePart("bold", diaryTitle),
                    new MessagePart("text", " 일기에 제공한 교정에 답글을 추가했습니다.")
            );
        }
    }

}

