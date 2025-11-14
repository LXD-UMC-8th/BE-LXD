package org.lxdproject.lxd.domain.member.repository;

import org.lxdproject.lxd.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    Boolean existsByEmail(String email);
    Boolean existsByNickname(String nickname);
    Boolean existsByUsername(String username);

    Optional<Member> findByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Member m
    SET m.isPurged = true,
        m.nickname = CONCAT('deleted_', m.id),
        m.email = CONCAT('deleted_', m.id, '@deleted.local')
    WHERE m.deletedAt IS NOT NULL
      AND m.deletedAt <= :threshold
      AND m.isPurged = false
    """)
    void hardDeleteMembersOlderThanThreshold(@Param("threshold") LocalDateTime threshold);

}
