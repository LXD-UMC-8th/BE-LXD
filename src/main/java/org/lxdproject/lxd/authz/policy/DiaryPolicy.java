package org.lxdproject.lxd.authz.policy;

import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.diary.entity.Diary;
import org.springframework.stereotype.Component;

@Component
public class DiaryVisibilityPolicy {
    public Permit canView(Long viewerId, Diary diary, boolean areFriends) {
        Long ownerId = diary.getMember().getId();
        if (diary.getMember().isDeleted()) return Permit.WITHDRAWN;

        if (viewerId != null && viewerId.equals(ownerId)) return Permit.ALLOW;

        return switch (diary.getVisibility()) {
            case PUBLIC   -> Permit.ALLOW;
            case FRIENDS  -> areFriends ? Permit.ALLOW : Permit.DENY;
            case PRIVATE  -> Permit.DENY;
        };
    }
}