package org.lxdproject.lxd.diarycommentlike.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.lxdproject.lxd.diarycommentlike.dto.DiaryCommentLikeResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "DiaryCommentLike", description = "일기 댓글 좋아요 API")
public interface DiaryCommentLikeApi {

    @Operation(
            summary = "댓글 좋아요 토글",
            description = "일기 댓글에 대해 좋아요 또는 좋아요 취소를 토글합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "요청 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
            }
    )
    @PostMapping("/diaries/{diaryId}/comments/{commentId}/likes")
    ResponseEntity<DiaryCommentLikeResponseDTO> toggleCommentLike(
            @Parameter(description = "일기 ID", example = "1")
            @PathVariable Long diaryId,

            @Parameter(description = "댓글 ID", example = "15")
            @PathVariable Long commentId
    );
}
