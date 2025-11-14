package org.lxdproject.lxd.domain.member.strategy.softDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.diarylike.repository.DiaryLikeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DiaryLikeSoftDeleteStrategy implements SoftDeleteStrategy {

    private final DiaryLikeRepository diaryLikeRepository;

    @Override
    public void softDelete(Long memberId, LocalDateTime deletedAt) {
        // 탈퇴자가 누른 일기 좋아요 및 탈퇴자가 작성한 일기가 받은 좋아요 soft delete
        diaryLikeRepository.softDeleteDiaryLikes(memberId, deletedAt);
    }
}
