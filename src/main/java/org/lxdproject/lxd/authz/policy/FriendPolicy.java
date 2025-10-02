package org.lxdproject.lxd.authz.policy;

import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.friend.repository.FriendRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class FriendPolicy {

    private final FriendRepository friendRepository;

    public FriendPolicy(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    public Permit validateSameMember(Member memberA, Member memberB) {
        if (memberA == null || memberB == null) return Permit.DENY;

        // 동일 인물인지 확인
        if(memberA.getId().equals(memberB.getId())) return Permit.DENY;

        return Permit.ALLOW;
    }

    public Permit validateFriends(Member memberA, Member memberB) {
        if (memberA == null || memberB == null) return Permit.DENY;

        // 이미 친구인지 확인
        boolean areFriends = friendRepository.areFriends(memberA.getId(), memberB.getId());
        if (areFriends) return Permit.DENY;

        return Permit.ALLOW;
    }

}
