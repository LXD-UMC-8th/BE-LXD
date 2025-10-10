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

    // 친구 관계가 존재하지 않아야 함 (요청 전 검사용)
    public Permit validateNotFriends(Member memberA, Member memberB) {
        if (memberA == null || memberB == null) return Permit.DENY;

        // 두 사용자가 이미 친구라면 DENY, 아니라면 ALLOW
        boolean areFriends = friendRepository.areFriends(memberA.getId(), memberB.getId());
        if (areFriends) return Permit.DENY;

        return Permit.ALLOW;
    }

    // 친구 관계가 반드시 존재해야 함 (취소,거절 등 관리 전 검사용)
    public Permit validateExistingFriends(Member memberA, Member memberB) {
        if (memberA == null || memberB == null) return Permit.DENY;

        // 두 사용자가 이미 친구라면 ALLOW, 아니라면 DENY
        boolean areFriends = friendRepository.areFriends(memberA.getId(), memberB.getId());
        return areFriends ? Permit.ALLOW : Permit.DENY;
    }

}
