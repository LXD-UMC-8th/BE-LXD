package org.lxdproject.lxd.diary.repository.DiaryRepository;

import org.lxdproject.lxd.diary.dto.DiarySliceResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryStatsResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiaryRepositoryCustom {
    DiarySliceResponseDTO findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable);
    List<DiaryStatsResponseDTO> getDiaryStatsByMonth(Long userId, int year, int month);
}

