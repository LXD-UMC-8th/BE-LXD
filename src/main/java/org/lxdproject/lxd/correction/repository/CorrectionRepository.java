package org.lxdproject.lxd.correction.repository;

import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    Page<Correction> findByAuthor(Member author, Pageable pageable);
}