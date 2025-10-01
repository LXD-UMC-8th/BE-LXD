package org.lxdproject.lxd.authz.policy;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DiaryPolicy {

    public Permit hasVisibilityPermission(Long viewerId, Diary diary, boolean areFriends) {

        Long ownerId = diary.getMember().getId();
        if (viewerId != null && viewerId.equals(ownerId)) return Permit.ALLOW;

        return switch (diary.getVisibility()) {
            case PUBLIC   -> Permit.ALLOW;
            case FRIENDS  -> areFriends ? Permit.ALLOW : Permit.DENY;
            case PRIVATE  -> Permit.DENY;
        };
    }

}