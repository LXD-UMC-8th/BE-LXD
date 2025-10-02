package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.FriendHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.FriendPolicy;
import org.lxdproject.lxd.authz.policy.MemberPolicy;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FriendGuard {
    private final FriendPolicy friendPolicy;

    public void validateFriendRequest(Member request, Member receiver) {
        // 동일인 검사
        if (friendPolicy.validateSameMember(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        // 이미 친구인지 검사
        if (friendPolicy.validateFriends(request, receiver) == Permit.DENY) {
            throw new FriendHandler(ErrorStatus.ALREADY_FRIENDS);
        }
    }

}
