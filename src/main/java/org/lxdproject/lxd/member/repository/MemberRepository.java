package org.lxdproject.lxd.member.repository;

import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Boolean existsByEmail(String email);
    Boolean existsByNickname(String nickname);
}
