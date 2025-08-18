package org.lxdproject.lxd.diary.repository;

import org.lxdproject.lxd.diary.dto.DiarySliceResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryStatsResponseDTO;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface DiaryRepositoryCustom {
    Page<Diary> findMyDiaries(Long memberId, Boolean likedOnly, Pageable pageable);
    Page<Diary> findDiariesByMemberId (Long viewerId, Long ownerId, Set<Long> friendIds, Pageable pageable);
    List<DiaryStatsResponseDTO> findDiaryStatsByMonth(Long userId, int year, int month);
    DiarySliceResponseDTO findDiariesOfFriends(Long userId, Pageable pageable);
    DiarySliceResponseDTO findLikedDiaries(Long userId, Pageable pageable);
    Page<Diary> findExploreDiaries(Long memberId, Language language, Set<Long> friendIds, Pageable pageable);
}

