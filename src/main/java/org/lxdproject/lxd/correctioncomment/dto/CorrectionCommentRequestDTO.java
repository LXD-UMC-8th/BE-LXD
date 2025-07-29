package org.lxdproject.lxd.correctioncomment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class CorrectionCommentRequestDTO {

    @Schema(description = "댓글 내용", example = "정말 좋은 교정이에요!")
    private Long parentId; // 대댓글일 경우 필요
    private String commentText;
}

