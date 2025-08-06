package org.lxdproject.lxd.member.repository;

import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FriendRepository {
    Page<Member> findFriendsByMemberId(Long memberId, Pageable pageable);
    long countFriendsByMemberId(Long memberId);
    boolean existsFriendshipByRequesterAndReceiver(Member m1, Member m2);
    void saveFriendship(Member requester, Member receiver);
    void softDeleteFriendship(Member m1, Member m2);
    boolean existsFriendRelation(Long memberId, Long friendId);
}
