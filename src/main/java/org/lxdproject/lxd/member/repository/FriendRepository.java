package org.lxdproject.lxd.member.repository;

import org.lxdproject.lxd.member.entity.Member;

import java.util.List;

public interface FriendRepository {
    List<Member> findFriendsByMemberId(Long memberId);
    boolean existsByRequesterAndReceiverOrReceiverAndRequester(Member m1, Member m2);
    void saveFriendship(Member requester, Member receiver);
    void softDeleteFriendship(Member m1, Member m2);
    boolean existsFriendRelation(Long memberId, Long friendId);
    Long countFriendsByMemberId(Long memberId);
}
