package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.DiaryVisibilityPolicy;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionGuard {

    private final FriendshipQueryPort friendshipQueryPort;
    private final DiaryVisibilityPolicy diaryVisibilityPolicy;

    public boolean canViewDiary(Long viewerId, Diary diary) {
        Long ownerId = diary.getMember().getId();
        boolean areFriends = viewerId != null && friendshipQueryPort.areFriends(viewerId, ownerId);

        Permit permit = diaryVisibilityPolicy.canView(viewerId, diary, areFriends);
        return permit == Permit.ALLOW;
    }

}