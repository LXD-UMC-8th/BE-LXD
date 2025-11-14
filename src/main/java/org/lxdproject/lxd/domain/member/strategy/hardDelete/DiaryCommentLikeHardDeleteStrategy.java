package org.lxdproject.lxd.domain.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(HardDeleteOrder.DIARY_COMMENT_LIKE)
public class DiaryCommentLikeHardDeleteStrategy implements HardDeleteStrategy {

    private final DiaryCommentLikeRepository diaryCommentLikeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void hardDelete(LocalDateTime threshold) {
        diaryCommentLikeRepository.hardDeleteDiaryCommentLikesOlderThanThreshold(threshold);
    }
}
