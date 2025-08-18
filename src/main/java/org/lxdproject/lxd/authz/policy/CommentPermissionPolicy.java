package org.lxdproject.lxd.authz.policy;

import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.diary.entity.Diary;
import org.springframework.stereotype.Component;

@Component
public class CommentPermissionPolicy {
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
}
