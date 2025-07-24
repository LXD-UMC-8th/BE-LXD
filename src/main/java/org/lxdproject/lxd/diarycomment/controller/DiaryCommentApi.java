package org.lxdproject.lxd.diarycomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentRequestDTO;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "일기 댓글", description = "Diary Comment API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries/{diaryId}/comments")
public class DiaryCommentApi {

    private final DiaryCommentController diaryCommentController;


    //댓글 작성
    @Operation(summary = "댓글 작성", description = "일기에 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<DiaryCommentResponseDTO>> writeComment(
            @PathVariable Long diaryId,
            @RequestBody DiaryCommentRequestDTO request
    ) {
        return diaryCommentController.writeComment(diaryId, request);
    }

    //댓글조회
    @GetMapping
    @Operation(summary = "댓글 조회", description = "해당 일기의 댓글들을 생성일 기준으로 조회합니다.")
    public ResponseEntity<ApiResponse<DiaryCommentResponseDTO.CommentList>> getComments(
            @PathVariable Long diaryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Member currentMember
    ) {
        return diaryCommentController.getDiaryComments(diaryId, page, size, currentMember);
    }

}







