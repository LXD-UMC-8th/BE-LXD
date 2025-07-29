package org.lxdproject.lxd.correctioncomment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CorrectionCommentRequestDTO {

    @Schema(description = "부모 댓글 ID (대댓글인 경우 사용)", example = "null")
    private Long parentId;

    @Schema(description = "댓글 본문", example = "정말 좋은 교정이에요!")
    private String commentText;
}




