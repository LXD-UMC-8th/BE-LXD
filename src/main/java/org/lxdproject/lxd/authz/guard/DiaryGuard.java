package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.DiaryPolicy;
import org.lxdproject.lxd.authz.policy.MemberPolicy;
import org.lxdproject.lxd.diary.entity.Diary;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DiaryGuard {
    private final DiaryPolicy diaryPolicy;
    private final MemberPolicy memberPolicy;
    private final FriendshipQueryPort friendshipQueryPort;

    public void canView(Long viewerId, Diary diary) {

        // 탈퇴한 사용자의 일기 조회 요청인지 검사
        Permit ownerPermit = memberPolicy.canUse(diary.getMember());
        if (ownerPermit == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.RESOURCE_OWNER_WITHDRAWN);
        }

        Long ownerId = diary.getMember().getId();
        boolean areFriends = viewerId != null && friendshipQueryPort.areFriends(viewerId, ownerId);

        Permit permit = diaryPolicy.canView(viewerId, diary, areFriends);
        if (permit == Permit.DENY) {
            throw new DiaryHandler(ErrorStatus.DIARY_PERMISSION_DENIED);
        }
    }

}
