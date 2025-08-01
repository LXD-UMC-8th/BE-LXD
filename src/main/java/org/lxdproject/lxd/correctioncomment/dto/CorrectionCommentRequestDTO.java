package org.lxdproject.lxd.correctioncomment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "교정 댓글 작성 요청 DTO")
@Getter
@Setter
public class CorrectionCommentRequestDTO {

    @NotBlank(message = "댓글 본문은 비어 있을 수 없습니다.")
    @Schema(description = "댓글 본문", example = "정말 좋은 교정이에요!")
    private String content;

}






