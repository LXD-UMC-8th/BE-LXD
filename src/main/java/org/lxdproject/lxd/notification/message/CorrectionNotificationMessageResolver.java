package org.lxdproject.lxd.notification.message;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CorrectionNotificationMessageResolver implements NotificationMessageResolver {

    private final CorrectionRepository correctionRepository;

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.CORRECTION_ADDED;
    }

    @Override
    public String resolveMessage(NotificationRequestDTO dto, Member sender, Locale locale) {
        if (dto.getTargetType() != TargetType.CORRECTION) {
            throw new NotificationHandler(ErrorStatus.TARGET_TYPE_MISMATCH);
        }

        Correction correction = correctionRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));

        String diaryTitle = correction.getDiary().getTitle();

        LocalizedMessageTemplate template = new LocalizedMessageTemplate(
                "%s님이 \"%s\" 일기에 교정을 추가했습니다",
                "%s suggested a correction to your diary \"%s\""
        );

        return template.format(sender.getNickname(), diaryTitle, locale);
    }
}
