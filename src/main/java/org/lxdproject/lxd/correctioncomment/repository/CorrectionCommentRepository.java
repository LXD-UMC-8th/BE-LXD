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
    @Query("SELECT cc.correction.diary.title FROM CorrectionComment cc WHERE cc.id = :id AND cc.correction IS NOT NULL AND cc.correction.diary IS NOT NULL")
    Optional<String> findDiaryTitleByCorrectionCommentId(@Param("id") Long id);
}

