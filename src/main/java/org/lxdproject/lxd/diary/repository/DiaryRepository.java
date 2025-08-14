package org.lxdproject.lxd.diary.repository;

import org.lxdproject.lxd.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom {
    Optional<Diary> findByIdAndDeletedAtIsNull(Long id);
    Long countByMemberIdAndDeletedAtIsNull(Long memberId);
}
