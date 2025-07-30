package org.lxdproject.lxd.correctioncomment.repository;

import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CorrectionCommentRepository extends JpaRepository<CorrectionComment, Long> {
    Page<CorrectionComment> findAllByCorrectionId(Long correctionId, Pageable pageable);
    @Query("SELECT cc.correction.diary.title FROM CorrectionComment cc WHERE cc.id = :id")
    Optional<String> findDiaryTitleByCorrectionCommentId(@Param("id") Long id);
}

