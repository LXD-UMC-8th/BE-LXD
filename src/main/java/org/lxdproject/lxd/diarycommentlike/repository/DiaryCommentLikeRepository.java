package org.lxdproject.lxd.diarycommentlike.repository;

import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycommentlike.entity.DiaryCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryCommentLikeRepository extends JpaRepository<DiaryCommentLike, Long> {
    Optional<DiaryCommentLike> findByMemberIdAndComment_Id(Long memberId, Long commentId);
    // 좋아요 여부 확인용
    boolean existsByCommentAndMemberId(DiaryComment comment, Long memberId);

    // 좋아요 토글용
    DiaryCommentLike findByCommentAndMemberId(DiaryComment comment, Long memberId);
}


