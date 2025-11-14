package org.lxdproject.lxd.domain.diarycomment.repository;

import org.lxdproject.lxd.domain.diarycomment.entity.DiaryComment;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryCommentRepositoryCustom {
    List<DiaryComment> findParentComments(Long diaryId, int offset, int size);
    List<DiaryComment> findRepliesByParentIds(List<Long> parentIds);
    Long countParentComments(Long diaryId);
    void softDeleteMemberComments(Long memberId, LocalDateTime deletedAt);
    void hardDeleteDiaryCommentsOlderThanThreshold(LocalDateTime threshold);

    void recoverDiaryCommentsByMemberIdAndDeletedAt(Long memberId, LocalDateTime deletedAt);
}
