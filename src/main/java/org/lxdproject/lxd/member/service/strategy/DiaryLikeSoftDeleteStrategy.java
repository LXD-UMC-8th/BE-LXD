package org.lxdproject.lxd.member.service.strategy;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarylike.repository.DiaryLikeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DiaryLikeSoftDeleteStrategy implements SoftDeleteStrategy {

    private final DiaryLikeRepository diaryLikeRepository;

    @Override
    public void softDelete(Long memberId, LocalDateTime deletedAt) {
        diaryLikeRepository.softDeleteDiaryLikes(memberId, deletedAt);
    }
}
