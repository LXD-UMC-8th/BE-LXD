package org.lxdproject.lxd.global.authz.policy;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.authz.model.Permit;
import org.lxdproject.lxd.domain.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.domain.diary.entity.Diary;
import org.lxdproject.lxd.domain.diarycomment.entity.DiaryComment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentPolicy {

    public Permit hasCommentPermission(Long writerId, Diary diary, boolean areFriends) {
        if (writerId == null) return Permit.DENY;

        Long ownerId = diary.getMember().getId();
        switch (diary.getCommentPermission()) {
            case NONE -> {
                return writerId.equals(ownerId) ? Permit.ALLOW : Permit.DENY;
            }
            case ALL     -> { return Permit.ALLOW; }
            case FRIENDS -> {
                if (writerId.equals(ownerId) || areFriends) return Permit.ALLOW;
                else return Permit.DENY;
            }
            default -> { return Permit.DENY; }
        }
    }

    private Permit canDeleteInternal(Long requesterId, Long writerId, Long ownerId) {
        if (requesterId == null) return Permit.DENY;

        return (requesterId.equals(writerId) || requesterId.equals(ownerId))
                ? Permit.ALLOW
                : Permit.DENY;
    }

    public Permit canDelete(Long requesterId, DiaryComment comment) {
        Long commentAuthorId = comment.getMember().getId();
        Long diaryOwnerId = comment.getDiary().getMember().getId();

        return canDeleteInternal(requesterId, commentAuthorId, diaryOwnerId);
    }

    public Permit canDelete(Long requesterId, CorrectionComment comment) {
        Long commentAuthorId = comment.getMember().getId();
        Long correctionOwnerId = comment.getCorrection().getAuthor().getId();

        return canDeleteInternal(requesterId, commentAuthorId, correctionOwnerId);
    }

}
