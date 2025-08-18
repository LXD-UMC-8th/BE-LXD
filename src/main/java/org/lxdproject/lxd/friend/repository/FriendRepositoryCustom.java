package org.lxdproject.lxd.friend.repository;

import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface FriendRepositoryCustom {
    Page<Member> findFriendsByMemberId(Long memberId, Pageable pageable);
    long countFriendsByMemberId(Long memberId);
    boolean existsFriendshipByRequesterAndReceiver(Member m1, Member m2);
    void saveFriendship(Member requester, Member receiver);
    void softDeleteFriendship(Member m1, Member m2);
    boolean existsFriendRelation(Long memberId, Long friendId);
    Set<Long> findFriendIdsByMemberId(Long memberId);
}
