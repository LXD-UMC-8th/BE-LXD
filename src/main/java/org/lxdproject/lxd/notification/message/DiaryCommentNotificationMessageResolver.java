package org.lxdproject.lxd.notification.message;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DiaryCommentNotificationMessageResolver implements NotificationMessageResolver {

    private final DiaryCommentRepository diaryCommentRepository;

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.COMMENT_ADDED;
    }

    @Override
    public String resolveMessage(NotificationRequestDTO dto, Member sender, Locale locale) {
        if (dto.getTargetType() != TargetType.DIARY_COMMENT) {
            throw new NotificationHandler(ErrorStatus.TARGET_TYPE_MISMATCH);
        }

        DiaryComment comment = diaryCommentRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        LocalizedMessageTemplate template = new LocalizedMessageTemplate(
                "%s님이 \"%s\" 일기에 댓글을 작성했습니다",
                "%s commented on your diary \"%s\""
        );

        return template.format(sender.getNickname(), comment.getDiary().getTitle(), locale);
    }
}

