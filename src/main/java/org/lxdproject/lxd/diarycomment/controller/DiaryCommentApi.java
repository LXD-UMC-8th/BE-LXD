package org.lxdproject.lxd.diarycomment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.diarycomment.dto.*;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "일기 댓글", description = "Diary Comment API")
@RequestMapping("/diaries/{diaryId}/comments")
public interface DiaryCommentApi {

    @Operation(summary = "댓글 작성", description = "일기에 댓글을 작성합니다.")
    @PostMapping
    ResponseEntity<ApiResponse<DiaryCommentResponseDTO>> writeComment(
            @PathVariable Long diaryId,
            @RequestBody DiaryCommentRequestDTO request
    );

    @Operation(summary = "댓글 조회", description = "해당 일기의 댓글들을 생성일 기준으로 조회합니다.")
    @GetMapping
    ResponseEntity<ApiResponse<DiaryCommentResponseDTO.CommentList>> getComments(
            @PathVariable Long diaryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Member currentMember
    );

    @Operation(summary = "댓글 삭제", description = "댓글 또는 대댓글을 소프트 삭제합니다.")
    @DeleteMapping("/{commentId}")
    ResponseEntity<ApiResponse<DiaryCommentDeleteResponseDTO>> deleteComment(
            @PathVariable Long diaryId,
            @PathVariable Long commentId
    );
}








