package org.lxdproject.lxd.correctioncomment.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.correctioncomment.dto.*;
import org.lxdproject.lxd.correctioncomment.service.CorrectionCommentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/corrections/{correctionId}/comments")
public class CorrectionCommentController {

    private final CorrectionCommentService correctionCommentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CorrectionCommentResponseDTO>> writeComment(
            @PathVariable Long correctionId,
            @RequestBody CorrectionCommentRequestDTO request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        CorrectionCommentResponseDTO response = correctionCommentService.createComment(correctionId, request, memberId);
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CorrectionCommentPageResponseDTO>> getComments(
            @PathVariable Long correctionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        CorrectionCommentPageResponseDTO response = correctionCommentService.getComments(correctionId, pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CorrectionCommentDeleteResponseDTO>> deleteComment(
            @PathVariable Long correctionId,
            @PathVariable Long commentId
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        CorrectionCommentDeleteResponseDTO response = correctionCommentService.deleteComment(correctionId, commentId, memberId);
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
    }
}

