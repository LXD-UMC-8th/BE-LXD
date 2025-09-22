package org.lxdproject.lxd.authz.policy;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentPolicy {

    private final MemberPolicy memberPolicy;

    public Permit canCreate(Long writerId, Diary diary, boolean areFriends) {
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

    private Permit canDeleteInternal(Long requesterId, Long writerId) {
        if (requesterId == null) return Permit.DENY;
        return requesterId.equals(writerId) ? Permit.ALLOW : Permit.DENY;
    }

    public Permit canDelete(Long requesterId, DiaryComment comment) {
        return canDeleteInternal(requesterId, comment.getMember().getId());
    }

    public Permit canDelete(Long requesterId, CorrectionComment comment) {
        return canDeleteInternal(requesterId, comment.getMember().getId());
    }

}
