package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(2)
public class DiaryCommentHardDeleteStrategy implements HardDeleteStrategy {

    private final DiaryCommentRepository diaryCommentRepository;

    @Override
    public void hardDelete(LocalDateTime threshold) {
        diaryCommentRepository.hardDeleteDiaryCommentsOlderThanThreshold(threshold);
    }
}
