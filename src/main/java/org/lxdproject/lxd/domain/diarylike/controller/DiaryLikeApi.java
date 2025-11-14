package org.lxdproject.lxd.domain.diarylike.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.global.apiPayload.ApiResponse;
import org.lxdproject.lxd.domain.diarylike.dto.DiaryLikeResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Tag(name = "Diary Like", description = "일기 좋아요 관련 API 입니다.")
@RequestMapping("/diaries")
public interface DiaryLikeApi {

    @PostMapping("/{diaryId}/likes")
    @Operation(summary = "일기 좋아요 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일기 좋아요 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 리소스입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ApiResponse<DiaryLikeResponseDTO.ToggleDiaryLikeResponseDTO> toggleDiaryLike(
            @Parameter(description = "일기 ID") @PathVariable Long diaryId
    );
}
