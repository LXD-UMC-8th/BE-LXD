package org.lxdproject.lxd.member.repository;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface FriendRepository {
    List<Member> findFriendsByMemberId(Long memberId);
}
