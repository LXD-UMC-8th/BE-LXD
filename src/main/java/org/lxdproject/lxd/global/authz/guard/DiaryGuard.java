package org.lxdproject.lxd.global.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.global.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.global.authz.model.Permit;
import org.lxdproject.lxd.global.authz.policy.DiaryPolicy;
import org.lxdproject.lxd.domain.diary.entity.Diary;
import org.lxdproject.lxd.domain.friend.adapter.FriendshipQueryPort;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DiaryGuard {
    private final DiaryPolicy diaryPolicy;
    private final FriendshipQueryPort friendshipQueryPort;

    public void hasVisibilityPermission(Long viewerId, Diary diary) {
        Long ownerId = diary.getMember().getId();
        boolean areFriends = viewerId != null && friendshipQueryPort.areFriends(viewerId, ownerId);

        Permit permit = diaryPolicy.hasVisibilityPermission(viewerId, diary, areFriends);
        if (permit == Permit.DENY) {
            throw new DiaryHandler(ErrorStatus.DIARY_PERMISSION_DENIED);
        }
    }

}
