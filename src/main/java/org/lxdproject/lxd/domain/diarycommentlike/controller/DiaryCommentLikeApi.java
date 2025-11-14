package org.lxdproject.lxd.domain.diarycommentlike.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.lxdproject.lxd.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.domain.diarycommentlike.dto.DiaryCommentLikeResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Tag(name = "Diary Comment Like", description = "일기 댓글 좋아요 관련 API 입니다.")
@RequestMapping("/diaries/comments/{commentId}/likes")
public interface DiaryCommentLikeApi {

    @PostMapping
    @Operation(summary = "댓글 좋아요 API")
    @Parameters({
            @Parameter(name = "commentId", description = "좋아요 할 댓글의 ID", required = true)
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일기 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 리소스입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ApiResponse<DiaryCommentLikeResponseDTO> toggleCommentLike(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId
    );

}


