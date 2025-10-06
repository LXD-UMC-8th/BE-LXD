package org.lxdproject.lxd.diarycommentlike.repository;

import java.time.LocalDateTime;

public interface DiaryCommentLikeRepositoryCustom {

    void softDeleteDiaryCommentLikes(Long memberId, LocalDateTime localDateTime);

    void hardDeleteDiaryCommentLikesOlderThanThreshold(LocalDateTime threshold);
}
