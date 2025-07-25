package org.lxdproject.lxd.diary.repository.DiaryRepository;

import org.lxdproject.lxd.diary.dto.DiarySliceResponseDto;
import org.springframework.data.domain.Pageable;

public interface DiaryRepositoryCustom {
    DiarySliceResponseDto findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable);
}

