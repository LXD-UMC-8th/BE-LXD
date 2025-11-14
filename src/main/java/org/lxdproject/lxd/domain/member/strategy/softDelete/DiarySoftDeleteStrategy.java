package org.lxdproject.lxd.domain.member.strategy.softDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.diary.repository.DiaryRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DiarySoftDeleteStrategy implements SoftDeleteStrategy {

    private final DiaryRepository diaryRepository;

    @Override
    public void softDelete(Long memberId, LocalDateTime deletedAt) {
        // 일기 soft delete
        diaryRepository.softDeleteDiariesByMemberId(memberId, deletedAt);
    }
}
