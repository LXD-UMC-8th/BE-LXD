package org.lxdproject.lxd.correction.repository;

import jakarta.persistence.LockModeType;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    Slice<Correction> findByAuthor(Member author, Pageable pageable);
    Slice<Correction> findByDiaryId(Long diaryId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Correction c WHERE c.id = :id")
    Optional<Correction> findByIdWithPessimisticLock(@Param("id") Long id);

    @Query("SELECT c.diary.title FROM Correction c WHERE c.id = :id")
    Optional<String> findDiaryTitleByCorrectionId(@Param("id") Long id);
}