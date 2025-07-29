package org.lxdproject.lxd.correctioncomment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CorrectionCommentDeleteResponseDTO {
    private Long commentId;
    private boolean isDeleted;
    private String content;
    private LocalDateTime deletedAt;
}

