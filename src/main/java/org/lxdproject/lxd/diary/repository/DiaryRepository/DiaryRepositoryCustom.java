package org.lxdproject.lxd.diary.repository.DiaryRepository;

import org.lxdproject.lxd.diary.dto.DiarySummaryResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface DiaryRepositoryCustom {
    Slice<DiarySummaryResponseDto> findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable);
}

