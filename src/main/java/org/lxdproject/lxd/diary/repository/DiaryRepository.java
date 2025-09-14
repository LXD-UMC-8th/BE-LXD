package org.lxdproject.lxd.diary.repository;

import org.lxdproject.lxd.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom {
    Optional<Diary> findByIdAndDeletedAtIsNull(Long id);
    Long countByMemberIdAndDeletedAtIsNull(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Diary d SET d.deletedAt = :deletedAt WHERE d.member.id = :memberId")
    void softDeleteDiariesByMemberId(@Param("memberId") Long memberId, @Param("deletedAt") LocalDateTime deletedAt);
}
