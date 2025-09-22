package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.*;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.CommentPolicy;
import org.lxdproject.lxd.authz.policy.DiaryPolicy;
import org.lxdproject.lxd.authz.policy.FriendPolicy;
import org.lxdproject.lxd.authz.policy.MemberPolicy;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionGuard {

    private final FriendshipQueryPort friendshipQueryPort;
    private final DiaryPolicy diaryPolicy;
    private final CommentPolicy policy;
    private final FriendPolicy friendPolicy;
    private final MemberPolicy memberPolicy;

    public void canViewDiary(Long viewerId, Diary diary) {
        Long ownerId = diary.getMember().getId();
        boolean areFriends = viewerId != null && friendshipQueryPort.areFriends(viewerId, ownerId);

        Permit permit = diaryPolicy.canView(viewerId, diary, areFriends);
        if (permit == Permit.DENY) {
            throw new DiaryHandler(ErrorStatus.DIARY_PERMISSION_DENIED);
        }

        Permit ownerPermit = memberPolicy.canUse(diary.getMember());
        if (ownerPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.RESOURCE_OWNER_WITHDRAWN);
        }

    }

    public void canCreateDiaryComment(Long writerId, Diary diary, boolean areFriends) {
        Permit ownerPermit = memberPolicy.canUse(diary.getMember());
        if (ownerPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.RESOURCE_OWNER_WITHDRAWN);
        }

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

    public void canViewFriendList(Member member) {
        Permit memberPermit = memberPolicy.canUse(member);
        if (memberPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.TARGET_USER_WITHDRAWN);
        }

    }

    public void canSendFriendRequest(Member request, Member receiver) {

        Permit memberPermit = memberPolicy.canUse(request);
        if (memberPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.TARGET_USER_WITHDRAWN);
        }
        memberPermit = memberPolicy.canUse(receiver);
        if (memberPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.TARGET_USER_WITHDRAWN);
        }

        // 동일인 검사
        if (friendPolicy.validateSameMember(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        // 이미 친구인지 검사
        if (friendPolicy.validateFriends(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.ALREADY_FRIENDS);
        }

    }

    public void canAcceptFriendRequest(Member request, Member receiver) {

        Permit memberPermit = memberPolicy.canUse(request);
        if (memberPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.TARGET_USER_WITHDRAWN);
        }
        memberPermit = memberPolicy.canUse(receiver);
        if (memberPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.TARGET_USER_WITHDRAWN);
        }

        // 동일인 검사
        if (friendPolicy.validateSameMember(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        // 이미 친구인지 검사
        if (friendPolicy.validateFriends(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.ALREADY_FRIENDS);
        }

    }

    public void canDeleteFriend(Member current, Member target){

        Permit memberPermit = memberPolicy.canUse(current);
        if (memberPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.TARGET_USER_WITHDRAWN);
        }
        memberPermit = memberPolicy.canUse(target);
        if (memberPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.TARGET_USER_WITHDRAWN);
        }

    }

}