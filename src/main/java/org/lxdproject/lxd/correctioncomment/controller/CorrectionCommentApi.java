package org.lxdproject.lxd.correctioncomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.PageDTO;
import org.lxdproject.lxd.correctioncomment.dto.CorrectionCommentDeleteResponseDTO;
import org.lxdproject.lxd.correctioncomment.dto.CorrectionCommentRequestDTO;
import org.lxdproject.lxd.correctioncomment.dto.CorrectionCommentResponseDTO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Correction Comment API", description = "교정 댓글 관련 API 입니다.")
@RequestMapping("/corrections/{correctionId}/comments")
public interface CorrectionCommentApi {

    @PostMapping
    @Operation(summary = "교정 댓글 작성 API", description = "교정 댓글을 작성합니다.")
    ApiResponse<CorrectionCommentResponseDTO> writeComment(
            @PathVariable Long correctionId,
            @RequestBody @Valid CorrectionCommentRequestDTO request
    );

    @Operation(summary = "교정 댓글 조회 API", description = "교정 댓글 목록을 조회합니다.")
    @GetMapping
    ApiResponse<PageDTO<CorrectionCommentResponseDTO>> getComments(
            @PathVariable Long correctionId,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 개수", example = "10") @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "나의 교정 댓글 삭제 API", description = "교정 댓글을 소프트 삭제합니다.")
    @DeleteMapping("/{commentId}")
    ApiResponse<CorrectionCommentDeleteResponseDTO> deleteComment(
            @PathVariable Long correctionId,
            @PathVariable Long commentId
    );
}





