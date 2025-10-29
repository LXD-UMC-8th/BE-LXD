package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarylike.repository.DiaryLikeRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(1)
public class DiaryLikeHardDeleteStrategy implements HardDeleteStrategy {

    private final DiaryLikeRepository diaryLikeRepository;

    @Override
    public void hardDelete(LocalDateTime threshold) {
        diaryLikeRepository.hardDeleteDiaryLikesOlderThanThreshold(threshold);
    }
}
