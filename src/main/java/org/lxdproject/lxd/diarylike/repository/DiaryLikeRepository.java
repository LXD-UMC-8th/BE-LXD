package org.lxdproject.lxd.diarylike.repository;

import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diarylike.entity.DiaryLike;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long>, DiaryLikeRepositoryCustom {
    Optional<DiaryLike> findByMemberAndDiary(Member member, Diary diary);
    List<DiaryLike> findAllByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM DiaryLike WHERE member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

}
