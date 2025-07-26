package org.lxdproject.lxd.member.repository;

import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Boolean existsByEmail(String email);
    Boolean existsByNickname(String nickname);

    Optional<Member> findByEmail(String email);
}
