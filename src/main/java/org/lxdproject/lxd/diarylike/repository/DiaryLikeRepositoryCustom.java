package org.lxdproject.lxd.diarylike.repository;

import java.util.Set;

public interface DiaryLikeRepositoryCustom {
    Set<Long> findLikedDiaryIdSet(Long memberId);
}
