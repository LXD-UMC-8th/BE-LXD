package org.lxdproject.lxd.diary.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DiarySliceResponseDto {
    private List<DiarySummaryResponseDto> diaries;
    private int page;
    private int size;
    private boolean hasNext;
}