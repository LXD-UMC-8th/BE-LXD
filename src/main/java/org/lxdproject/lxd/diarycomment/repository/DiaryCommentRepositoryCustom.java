package org.lxdproject.lxd.diarycomment.repository;

import org.lxdproject.lxd.diarycomment.entity.DiaryComment;

import java.util.List;

public interface DiaryCommentRepositoryCustom {
    List<DiaryComment> findParentComments(Long diaryId, int offset, int size);
    List<DiaryComment> findRepliesByParentIds(List<Long> parentIds);
    Long countParentComments(Long diaryId);
}
