package org.lxdproject.lxd.diarycommentlike.repository;

import org.lxdproject.lxd.diarycommentlike.entity.DiaryCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryCommentLikeRepository extends JpaRepository<DiaryCommentLike, Long> {
    Optional<DiaryCommentLike> findByMemberIdAndComment_Id(Long memberId, Long commentId);
}
