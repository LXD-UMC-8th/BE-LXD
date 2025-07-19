package org.lxdproject.lxd.diarycomment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DiaryCommentResponseDTO {
    private Long id;
    private Long userId;
    private Long diaryId;
    private String commentText;
    private Long parentId;
    private LocalDateTime createdAt;
}

