package org.lxdproject.lxd.domain.diarycomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.domain.diarycomment.dto.DiaryCommentDeleteResponseDTO;
import org.lxdproject.lxd.domain.diarycomment.dto.DiaryCommentRequestDTO;
import org.lxdproject.lxd.domain.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.global.apiPayload.ApiResponse;
import org.lxdproject.lxd.global.common.dto.PageDTO;
import org.lxdproject.lxd.diarycomment.dto.*;
import org.lxdproject.lxd.global.validation.annotation.PageSizeValid;
import org.lxdproject.lxd.global.validation.annotation.PageValid;
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
    ApiResponse<PageDTO<DiaryCommentResponseDTO.Comment>> getComments(
            @PathVariable Long diaryId,
            @RequestParam(defaultValue = "1") @PageValid int page,
            @RequestParam(defaultValue = "10") @PageSizeValid int size
    );

    @Operation(summary = "일기 댓글 삭제 API", description = "댓글 또는 대댓글을 소프트 삭제합니다.")
    @DeleteMapping("/{commentId}")
    ApiResponse<DiaryCommentDeleteResponseDTO> deleteComment(
            @PathVariable Long diaryId,
            @PathVariable Long commentId
    );
}








