package org.lxdproject.lxd.domain.diarylike.repository;

import org.lxdproject.lxd.domain.diary.entity.Diary;
import org.lxdproject.lxd.domain.diarylike.entity.DiaryLike;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long>, DiaryLikeRepositoryCustom {
    Optional<DiaryLike> findByMemberAndDiary(Member member, Diary diary);
    boolean existsByMemberIdAndDiaryId(Long memberId, Long diaryId);
}
