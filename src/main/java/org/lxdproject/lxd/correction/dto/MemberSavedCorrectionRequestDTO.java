package org.lxdproject.lxd.correction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberSavedCorrectionRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "메모 요청 DTO")
    public static class MemoRequestDTO {

        @NotNull(message = "savedCorrectionId는 필수입니다.")
        @Schema(description = "회원이 저장한 교정 ID", example = "812")
        private Long savedCorrectionId;

        @NotBlank(message = "메모는 비어 있을 수 없습니다.")
        @Schema(description = "저장 교정 내 메모 내용", example = "이 부분 잊지말자!")
        private String memo;
    }
}
