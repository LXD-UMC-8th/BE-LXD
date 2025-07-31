package org.lxdproject.lxd.diarycomment.repository;

import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {
    Page<DiaryComment> findByDiaryIdAndParentIsNull(Long diaryId, Pageable pageable);
    List<DiaryComment> findByParentIdIn(List<Long> parentIds);

    @Query("SELECT COUNT(c) FROM DiaryComment c WHERE c.diary.id = :diaryId")
    long countAllCommentsIncludingDeleted(@Param("diaryId") Long diaryId);

}