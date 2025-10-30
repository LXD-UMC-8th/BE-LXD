package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(HardDeleteOrder.DIARY_COMMENT_LIKE)
public class DiaryCommentLikeHardDeleteStrategy implements HardDeleteStrategy {

    private final DiaryCommentLikeRepository diaryCommentLikeRepository;

    @Override
    public void hardDelete(LocalDateTime threshold) {
        diaryCommentLikeRepository.hardDeleteDiaryCommentLikesOlderThanThreshold(threshold);
    }
}
