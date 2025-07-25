package org.lxdproject.lxd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Visibility;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class DiarySummaryResponseDto {
    private Long diaryId;
    private LocalDateTime createdAt;
    private String title;
    private Visibility visibility;
    private String thumbnailUrl;
    private int likeCount;
    private int commentCount;
    private int correctionCount;
    private String contentPreview;
    private Language language;
}

