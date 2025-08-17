package org.lxdproject.lxd.diarycomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.PageResponse;
import org.lxdproject.lxd.diarycomment.dto.*;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Diary Comment API", description = "일기 댓글 관련 API 입니다.")
@RequestMapping("/diaries/{diaryId}/comments")
public interface DiaryCommentApi {

    @Operation(summary = "일기 댓글 작성 API", description = "일기에 댓글을 작성합니다.")
    @PostMapping
    ApiResponse<DiaryCommentResponseDTO> writeComment(
            @PathVariable Long diaryId,
            @RequestBody DiaryCommentRequestDTO request
    );

    @Operation(summary = "일기 댓글 조회 API", description = "해당 일기의 댓글들을 생성일 기준으로 조회합니다.")
    @GetMapping
    ApiResponse<PageResponse<DiaryCommentResponseDTO.Comment>> getComments(
            @PathVariable Long diaryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "일기 댓글 삭제 API", description = "댓글 또는 대댓글을 소프트 삭제합니다.")
    @DeleteMapping("/{commentId}")
    ApiResponse<DiaryCommentDeleteResponseDTO> deleteComment(
            @PathVariable Long diaryId,
            @PathVariable Long commentId
    );
}








