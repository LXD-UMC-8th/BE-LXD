package org.lxdproject.lxd.diary.repository;

import org.lxdproject.lxd.diary.dto.DiarySliceResponseDTO;
import org.lxdproject.lxd.diary.dto.MyDiarySliceResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryStatsResponseDTO;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiaryRepositoryCustom {
    MyDiarySliceResponseDTO findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable);
    MyDiarySliceResponseDTO getDiariesByMemberId (Long userId, Long memberId, Pageable pageable);
    List<DiaryStatsResponseDTO> getDiaryStatsByMonth(Long userId, int year, int month);
    DiarySliceResponseDTO findDiariesOfFriends(Long userId, Pageable pageable);
    DiarySliceResponseDTO findLikedDiariesOfFriends(Long userId, Pageable pageable);
    DiarySliceResponseDTO findExploreDiaries(Long userId, Language language, Pageable pageable);
}

