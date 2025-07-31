package org.lxdproject.lxd.correctioncomment.repository;

import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CorrectionCommentRepository extends JpaRepository<CorrectionComment, Long> {
    // 부모 댓글 페이징
    Page<CorrectionComment> findByCorrectionIdAndParentIsNull(Long correctionId, Pageable pageable);

    // 자식 댓글 일괄 조회
    List<CorrectionComment> findByCorrectionIdIn(List<Long> parentIds);
    List<CorrectionComment> findByParentIdIn(List<Long> parentIds);

    // 교정댓글 → 다이어리 제목 조회
    @Query("SELECT cc.correction.diary.title FROM CorrectionComment cc WHERE cc.id = :id AND cc.correction IS NOT NULL AND cc.correction.diary IS NOT NULL")
    Optional<String> findDiaryTitleByCorrectionCommentId(@Param("id") Long id);
}

