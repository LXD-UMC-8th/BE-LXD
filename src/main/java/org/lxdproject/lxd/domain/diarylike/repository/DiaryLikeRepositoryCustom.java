package org.lxdproject.lxd.domain.diarylike.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface DiaryLikeRepositoryCustom {
    Set<Long> findLikedDiaryIdSet(Long memberId);
    List<Long> findLikedDiaryIdList(Long memberId);
    void softDeleteDiaryLikes(Long memberId, LocalDateTime localDateTime);

    void hardDeleteDiaryLikesOlderThanThreshold(LocalDateTime threshold);

    void recoverDiaryLikesByMemberIdAndDeletedAt(Long memberId, LocalDateTime deletedAt);
}
