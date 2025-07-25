package org.lxdproject.lxd.diary.repository;

import org.lxdproject.lxd.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom {
}
