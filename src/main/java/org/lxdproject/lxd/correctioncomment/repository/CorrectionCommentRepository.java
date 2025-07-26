package org.lxdproject.lxd.correctioncomment.repository;

import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectionCommentRepository extends JpaRepository<CorrectionComment, Long> {
    Page<CorrectionComment> findAllByCorrectionId(Long correctionId, Pageable pageable);
}

