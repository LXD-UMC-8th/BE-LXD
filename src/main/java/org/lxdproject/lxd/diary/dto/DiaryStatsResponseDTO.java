package org.lxdproject.lxd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiaryStatsResponseDTO {
    private String date;
    private long count;
}