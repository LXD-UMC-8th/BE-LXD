package org.lxdproject.lxd.domain.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.diarylike.repository.DiaryLikeRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(HardDeleteOrder.DIARY_LIKE)
public class DiaryLikeHardDeleteStrategy implements HardDeleteStrategy {

    private final DiaryLikeRepository diaryLikeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void hardDelete(LocalDateTime threshold) {
        diaryLikeRepository.hardDeleteDiaryLikesOlderThanThreshold(threshold);
    }
}
