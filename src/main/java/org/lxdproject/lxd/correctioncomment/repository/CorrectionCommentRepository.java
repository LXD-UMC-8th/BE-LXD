package org.lxdproject.lxd.correctioncomment.repository;

import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CorrectionCommentRepository extends JpaRepository<CorrectionComment, Long> {

    @Query("SELECT c FROM CorrectionComment c WHERE c.correction.id = :correctionId ORDER BY c.createdAt ASC")
    Page<CorrectionComment> findByCorrectionIdWithOldestFirst(@Param("correctionId") Long correctionId, Pageable pageable);


    // 부모 댓글 페이징
    Page<CorrectionComment> findByCorrectionIdAndParentIsNull(Long correctionId, Pageable pageable);

    // 자식 댓글 일괄 조회
    List<CorrectionComment> findByCorrectionIdIn(List<Long> parentIds);

    List<CorrectionComment> findByParentIdIn(List<Long> parentIds);
}

