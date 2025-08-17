package org.lxdproject.lxd.diarycomment.repository;

import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long>, DiaryCommentRepositoryCustom {
    @Query("SELECT dc.diary.title FROM DiaryComment dc WHERE dc.id = :id AND dc.diary IS NOT NULL")
    Optional<String> findDiaryTitleByCommentId(@Param("id") Long id);

}