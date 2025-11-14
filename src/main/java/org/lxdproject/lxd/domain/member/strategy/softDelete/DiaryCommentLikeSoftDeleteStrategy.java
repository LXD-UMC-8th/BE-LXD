package org.lxdproject.lxd.domain.member.strategy.softDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DiaryCommentLikeSoftDeleteStrategy implements SoftDeleteStrategy {

    private final DiaryCommentLikeRepository diaryCommentLikeRepository;

    @Override
    public void softDelete(Long memberId, LocalDateTime deletedAt) {

        // 탈퇴자가 누른 일기 댓글 좋아요 및 탈퇴자가 작성한 댓글이 받은 좋아요 soft delete
        diaryCommentLikeRepository.softDeleteDiaryCommentLikes(memberId, deletedAt);
    }

}
