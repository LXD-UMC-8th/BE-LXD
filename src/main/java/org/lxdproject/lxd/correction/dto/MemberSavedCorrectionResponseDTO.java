package org.lxdproject.lxd.correction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MemberSavedCorrectionResponseDTO {

    @Getter
    @Builder
    public static class CreateMemoResponseDTO {

        private Long correctionId;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class UpdateMemoResponseDTO {

        private String updatedMemo;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    public static class DeleteMemoResponseDTO {

        private Long correctionId;
        private LocalDateTime deletedAt;
        private String message;
    }
}
