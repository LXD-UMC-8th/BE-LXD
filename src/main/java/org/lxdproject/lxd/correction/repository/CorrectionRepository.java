package org.lxdproject.lxd.correction.repository;

import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    Slice<Correction> findByAuthor(Member author, Pageable pageable);
    Slice<Correction> findByDiaryId(Long diaryId, Pageable pageable);
}