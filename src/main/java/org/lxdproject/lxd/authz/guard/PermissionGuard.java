package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.CommentPermissionPolicy;
import org.lxdproject.lxd.authz.policy.DiaryVisibilityPolicy;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionGuard {

    private final FriendshipQueryPort friendshipQueryPort;
    private final DiaryVisibilityPolicy diaryVisibilityPolicy;
    private final CommentPermissionPolicy policy;

    public void canViewDiary(Long viewerId, Diary diary) {
        Long ownerId = diary.getMember().getId();
        boolean areFriends = viewerId != null && friendshipQueryPort.areFriends(viewerId, ownerId);

        Permit permit = diaryVisibilityPolicy.canView(viewerId, diary, areFriends);
        if (permit == Permit.DENY || permit == Permit.WITHDRAWN) {
            throw new DiaryHandler(ErrorStatus.DIARY_PERMISSION_DENIED);
        }
    }

    public void canCreateDiaryComment(Long writerId, Diary diary, boolean areFriends) {
        Permit permit = policy.canCreate(writerId, diary, areFriends);
        if (permit == Permit.DENY) {
            throw new CommentHandler(ErrorStatus.COMMENT_PERMISSION_DENIED);
        }
    }

    public void canDeleteDiaryComment(Long requesterId, DiaryComment comment) {
        Permit permit = policy.canDelete(requesterId, comment);
        if (permit == Permit.DENY) {
            throw new CommentHandler(ErrorStatus.COMMENT_DELETE_PERMISSION_DENIED);
        }
    }

    public void canDeleteCorrectionComment(Long requesterId, CorrectionComment comment) {
        Permit permit = policy.canDelete(requesterId, comment);
        if (permit == Permit.DENY) {
            throw new CommentHandler(ErrorStatus.COMMENT_DELETE_PERMISSION_DENIED);
        }
    }

}