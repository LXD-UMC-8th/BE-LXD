package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.CommentPolicy;
import org.lxdproject.lxd.authz.policy.MemberPolicy;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentGuard {
    private final CommentPolicy commentPolicy;
    private final MemberPolicy memberPolicy;

    public void canCreateDiaryComment(Long writerId, Diary diary, boolean areFriends) {

        // 탈퇴한 사용자의 일기에 댓글 작성하는 요청인지 검사
        Permit ownerPermit = memberPolicy.canUse(diary.getMember());
        if (ownerPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.RESOURCE_OWNER_WITHDRAWN);
        }

        Permit permit = commentPolicy.canCreate(writerId, diary, areFriends);
        if (permit == Permit.DENY) {
            throw new CommentHandler(ErrorStatus.COMMENT_PERMISSION_DENIED);
        }
    }

    public void canDeleteDiaryComment(Long requesterId, DiaryComment comment) {
        Permit permit = commentPolicy.canDelete(requesterId, comment);
        if (permit == Permit.DENY) {
            throw new CommentHandler(ErrorStatus.COMMENT_DELETE_PERMISSION_DENIED);
        }
    }

    public void canDeleteCorrectionComment(Long requesterId, CorrectionComment comment) {
        Permit permit = commentPolicy.canDelete(requesterId, comment);
        if (permit == Permit.DENY) {
            throw new CommentHandler(ErrorStatus.COMMENT_DELETE_PERMISSION_DENIED);
        }
    }
}
