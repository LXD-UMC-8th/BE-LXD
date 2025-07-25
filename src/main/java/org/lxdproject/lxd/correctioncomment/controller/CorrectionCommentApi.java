package org.lxdproject.lxd.correctioncomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.correctioncomment.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CorrectionComment", description = "교정 댓글 API")
public interface CorrectionCommentApi {

    @Operation(summary = "교정 댓글 작성")
    @PostMapping("/corrections/{correctionId}/comments")
    ResponseEntity<ApiResponse<CorrectionCommentResponseDTO>> writeComment(
            @PathVariable Long correctionId,
            @RequestBody CorrectionCommentRequestDTO request
    );

    @Operation(summary = "교정 댓글 조회")
    @GetMapping("/corrections/{correctionId}/comments")
    ResponseEntity<ApiResponse<CorrectionCommentPageResponseDTO>> getComments(
            @PathVariable Long correctionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "교정 댓글 삭제 (Soft Delete)")
    @DeleteMapping("/corrections/{correctionId}/comments/{commentId}")
    ResponseEntity<ApiResponse<CorrectionCommentDeleteResponseDTO>> deleteComment(
            @PathVariable Long correctionId,
            @PathVariable Long commentId
    );
}

