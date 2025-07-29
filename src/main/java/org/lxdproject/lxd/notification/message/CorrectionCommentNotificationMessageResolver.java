package org.lxdproject.lxd.notification.message;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.correctioncomment.repository.CorrectionCommentRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CorrectionCommentNotificationMessageResolver implements NotificationMessageResolver {

    private final CorrectionCommentRepository correctionCommentRepository;

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.CORRECTION_REPLIED;
    }

    @Override
    public String resolveMessage(NotificationRequestDTO dto, Member sender, Locale locale) {
        if (dto.getTargetType() != TargetType.CORRECTION_COMMENT) {
            throw new NotificationHandler(ErrorStatus.TARGET_TYPE_MISMATCH);
        }

        CorrectionComment comment = correctionCommentRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        LocalizedMessageTemplate template = new LocalizedMessageTemplate(
                "%s님이 %s 일기에 제공한 교정에 답글을 추가했습니다.",
                "%s replied to the correction on the %s diary entry."
        );

        return template.format(sender.getNickname(), comment.getCorrection().getDiary().getTitle(), locale);
    }
}

