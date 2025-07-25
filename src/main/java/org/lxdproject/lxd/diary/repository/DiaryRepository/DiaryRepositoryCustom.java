package org.lxdproject.lxd.diary.repository.DiaryRepository;

import org.lxdproject.lxd.diary.dto.DiarySliceResponseDto;
import org.lxdproject.lxd.diary.dto.DiaryStatsResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiaryRepositoryCustom {
    DiarySliceResponseDto findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable);
    List<DiaryStatsResponseDto> getDiaryStatsByMonth(Long userId, int year, int month);
}

