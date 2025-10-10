package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.FriendHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.FriendPolicy;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FriendGuard {
    private final FriendPolicy friendPolicy;

    public void validateBeforeRequestAction(Member request, Member receiver) {
        // 동일인 검사
        if (friendPolicy.validateSameMember(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        // 이미 친구인지 검사
        // 두 사용자가 이미 친구라면 DENY, 아니라면 ALLOW
        if (friendPolicy.validateNotFriends(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.ALREADY_FRIENDS);
        }
    }

    public void validateBeforeManageAction(Member request, Member receiver) {
        // 동일인 검사
        if (friendPolicy.validateSameMember(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        // 이미 친구인지 검사
        // 두 사용자가 이미 친구라면 ALLOW, 아니라면 DENY
        if (friendPolicy.validateExistingFriends(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.NOT_FRIEND);
        }
    }

}
