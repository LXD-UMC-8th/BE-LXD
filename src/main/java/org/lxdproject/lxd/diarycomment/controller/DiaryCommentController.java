package org.lxdproject.lxd.diarycomment.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentRequestDTO;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.diarycomment.service.DiaryCommentService;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries/{diaryId}/comments")
public class DiaryCommentController {

    private final DiaryCommentService diaryCommentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DiaryCommentResponseDTO>> writeComment(
            @PathVariable Long diaryId,
            @RequestBody DiaryCommentRequestDTO request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        DiaryCommentResponseDTO response = diaryCommentService.writeComment(memberId, diaryId, request);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus._OK, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DiaryCommentResponseDTO.CommentList>> getDiaryComments(
            @PathVariable Long diaryId,
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal Member currentMember
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        DiaryCommentResponseDTO.CommentList response =
                diaryCommentService.getComments(diaryId, currentMember.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus._OK, response));
    }
}






