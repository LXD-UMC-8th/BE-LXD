package org.lxdproject.lxd.domain.diarycommentlike.repository;

import java.time.LocalDateTime;

public interface DiaryCommentLikeRepositoryCustom {

    void softDeleteDiaryCommentLikes(Long memberId, LocalDateTime localDateTime);

    void hardDeleteDiaryCommentLikesOlderThanThreshold(LocalDateTime threshold);

    void recoverDiaryCommentLikesByMemberIdAndDeletedAt(Long memberId, LocalDateTime deletedAt);
}
