package org.lxdproject.lxd.diarycomment.repository;

import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long>, DiaryCommentRepositoryCustom {
    @Query("SELECT dc.diary.title FROM DiaryComment dc WHERE dc.id = :id AND dc.diary IS NOT NULL")
    Optional<String> findDiaryTitleByCommentId(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DiaryComment dc SET dc.deletedAt = :deletedAt WHERE dc.diary.member.id = :memberId")
    void softDeleteDiaryCommentsByMemberId(@Param("memberId") Long memberId, @Param("deletedAt") LocalDateTime deletedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    DELETE FROM DiaryComment dc 
    WHERE dc.deletedAt IS NOT NULL 
      AND dc.deletedAt <= :threshold
    """)
    void deleteDiaryCommentsOlderThan30Days(@Param("threshold") LocalDateTime threshold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DiaryComment dc SET dc.deletedAt = NULL WHERE dc.member.id = :memberId AND dc.deletedAt = :deletedAt")
    void recoverDiaryCommentsByMemberIdAndDeletedAt(
            @Param("memberId") Long memberId,
            @Param("deletedAt") LocalDateTime deletedAt
    );
}