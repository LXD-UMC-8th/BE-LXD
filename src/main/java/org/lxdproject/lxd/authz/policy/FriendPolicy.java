package org.lxdproject.lxd.authz.policy;

import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
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

    // 동일 인물인지 확인
    public Permit validateSameMember(Member memberA, Member memberB) {
        if (memberA == null || memberB == null)
            throw new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND);

        if(memberA.getId().equals(memberB.getId())) return Permit.DENY;
        return Permit.ALLOW;
    }

    // 이미 친구인지 확인
    public Permit validateFriends(Member memberA, Member memberB) {
        if (memberA == null || memberB == null)
            throw new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND);

        boolean areFriends = friendRepository.areFriends(memberA.getId(), memberB.getId());

        if (areFriends) return Permit.DENY;
        return Permit.ALLOW;
    }

    // 탈퇴한 유저인지 확인
    public Permit validateDeletedMember(Member memberA, Member memberB) {
        if (memberA == null || memberB == null)
            throw new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND);

        boolean isDeleted = false;

        if(memberA.isDeleted() || memberB.isDeleted()) {
            isDeleted = true;
        }

        if (isDeleted) return Permit.WITHDRAWN;
        return Permit.ALLOW;
    }

}
