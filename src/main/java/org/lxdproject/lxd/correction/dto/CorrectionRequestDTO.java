package org.lxdproject.lxd.correction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CorrectionRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "교정 등록 요청 DTO")
    public static class CreateRequestDTO {

        @Schema(description = "대상 일기 ID", example = "42")
        private Long diaryId;

        @Schema(description = "원본 문장", example = "오늘은 피자날입니다.")
        private String original;

        @Schema(description = "교정한 내용", example = "피자 먹는날")
        private String corrected;

        @Schema(description = "피드백 코멘트", example = "‘피자날’보다는 ‘피자 먹는날’이 자연스럽습니다.")
        private String commentText;
    }
}
