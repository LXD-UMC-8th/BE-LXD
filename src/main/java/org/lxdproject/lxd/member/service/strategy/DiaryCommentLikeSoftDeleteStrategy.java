package org.lxdproject.lxd.member.service.strategy;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DiaryCommentLikeSoftDeleteStrategy implements SoftDeleteStrategy {

    private final DiaryCommentLikeRepository diaryCommentLikeRepository;

    @Override
    public void softDelete(Long memberId, LocalDateTime deletedAt) {
        diaryCommentLikeRepository.softDeleteDiaryCommentLikes(memberId, deletedAt);
    }

}
