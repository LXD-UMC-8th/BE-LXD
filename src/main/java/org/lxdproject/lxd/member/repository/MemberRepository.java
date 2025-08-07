package org.lxdproject.lxd.member.repository;

import org.lxdproject.lxd.friend.dto.FriendSearchResponseDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    Boolean existsByEmail(String email);
    Boolean existsByNickname(String nickname);
    Boolean existsByUsername(String username);

    Optional<Member> findByEmail(String email);
}
