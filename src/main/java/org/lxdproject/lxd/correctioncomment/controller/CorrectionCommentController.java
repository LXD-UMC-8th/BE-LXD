package org.lxdproject.lxd.correctioncomment.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.common.dto.PageDTO;
import org.lxdproject.lxd.correctioncomment.dto.*;
import org.lxdproject.lxd.correctioncomment.service.CorrectionCommentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class CorrectionCommentController implements CorrectionCommentApi {

    private final CorrectionCommentService correctionCommentService;

    @Override
    public ApiResponse<CorrectionCommentResponseDTO> writeComment(
            Long correctionId,
            CorrectionCommentRequestDTO request
    ) {
        CorrectionCommentResponseDTO response = correctionCommentService.writeComment(correctionId, request);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<PageDTO<CorrectionCommentResponseDTO>> getComments(Long correctionId, int page, int size) {
        PageDTO<CorrectionCommentResponseDTO> response = correctionCommentService.getComments(correctionId, page, size);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<CorrectionCommentDeleteResponseDTO> deleteComment(Long correctionId, Long commentId) {
        CorrectionCommentDeleteResponseDTO response = correctionCommentService.deleteComment(commentId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }
}





