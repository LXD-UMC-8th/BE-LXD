package org.lxdproject.lxd.diary.repository;

import org.lxdproject.lxd.diary.dto.DiarySliceResponseDTO;
import org.lxdproject.lxd.diary.dto.MyDiarySliceResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryStatsResponseDTO;
import org.lxdproject.lxd.diary.entity.Diary;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiaryRepositoryCustom {
    MyDiarySliceResponseDTO findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable);
    List<DiaryStatsResponseDTO> getDiaryStatsByMonth(Long userId, int year, int month);
    List<Diary> findByMemberIdAndVisibilityForViewer(Long memberId, boolean isFriend);
    DiarySliceResponseDTO findDiariesOfFriends(Long userId, Pageable pageable);
}

