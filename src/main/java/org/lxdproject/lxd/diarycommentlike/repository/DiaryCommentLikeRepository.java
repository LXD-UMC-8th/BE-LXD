package org.lxdproject.lxd.diarycommentlike.repository;

import org.springframework.data.repository.query.Param;
import org.lxdproject.lxd.diarycommentlike.entity.DiaryCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DiaryCommentLikeRepository extends JpaRepository<DiaryCommentLike, Long> {
    Optional<DiaryCommentLike> findByMemberIdAndCommentId(Long memberId, Long commentId);

    @Query("""
    SELECT l.comment.id FROM DiaryCommentLike l 
    WHERE l.member.id = :memberId 
    AND l.comment.id IN :commentIds
""")
    List<Long> findLikedCommentIds(@Param("memberId") Long memberId, @Param("commentIds") List<Long> commentIds);

}