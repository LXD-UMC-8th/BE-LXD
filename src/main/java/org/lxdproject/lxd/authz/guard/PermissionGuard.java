package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.FriendHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.CommentPermissionPolicy;
import org.lxdproject.lxd.authz.policy.DiaryVisibilityPolicy;
import org.lxdproject.lxd.authz.policy.FriendPolicy;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionGuard {

    private final FriendshipQueryPort friendshipQueryPort;
    private final DiaryVisibilityPolicy diaryVisibilityPolicy;
    private final CommentPermissionPolicy policy;
    private final FriendPolicy friendPolicy;

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

    public void canSendFriendRequest(Member request, Member receiver) {

        Permit permit = friendPolicy.validateDeletedMember(request, receiver);
        if(permit == Permit.WITHDRAWN) {
            throw new FriendHandler(ErrorStatus.WITHDRAWN_USER);
        }

        permit = friendPolicy.validateSameMember(request, receiver);
        if(permit == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        permit = friendPolicy.validateFriends(request, receiver);
        if(permit == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.ALREADY_FRIENDS);
        }

    }

    public void canAcceptFriendRequest(Member request, Member receiver) {

        Permit permit = friendPolicy.validateDeletedMember(request, receiver);
        if(permit == Permit.WITHDRAWN) {
            throw new FriendHandler(ErrorStatus.WITHDRAWN_USER);
        }

        permit = friendPolicy.validateSameMember(request, receiver);
        if(permit == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        permit = friendPolicy.validateFriends(request, receiver);
        if(permit == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.ALREADY_FRIENDS);
        }

    }

    public void canDeleteFriend(Member current, Member target){

        Permit permit = friendPolicy.validateDeletedMember(current, target);
        if(permit == Permit.WITHDRAWN) {
            throw new FriendHandler(ErrorStatus.WITHDRAWN_USER);
        }
    }


}