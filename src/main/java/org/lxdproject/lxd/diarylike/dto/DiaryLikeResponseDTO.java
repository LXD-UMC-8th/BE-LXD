package org.lxdproject.lxd.diarylike.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DiaryLikeResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToggleDiaryLikeResponseDTO {

        @Schema(description = "일기 ID")
        private Long diaryId;

        @Schema(description = "좋아요를 누른 사용자 ID")
        private Long memberId;

        @Schema(description = "좋아요 상태")
        private Boolean liked;

        @Schema(description = "해당 일기의 좋아요 수")
        private Integer likedCount;

    }

}
