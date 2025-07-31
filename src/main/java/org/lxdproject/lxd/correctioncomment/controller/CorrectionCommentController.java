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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CorrectionCommentController implements CorrectionCommentApi {

    private final CorrectionCommentService correctionCommentService;

    @Override
    public ApiResponse<CorrectionCommentResponseDTO> writeComment(
            Long correctionId,
            CorrectionCommentRequestDTO request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        CorrectionCommentResponseDTO response = correctionCommentService.writeComment(memberId, correctionId, request);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<CorrectionCommentPageResponseDTO> getComments(Long correctionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Long userId = SecurityUtil.getCurrentMemberId();
        CorrectionCommentPageResponseDTO response = correctionCommentService.getComments(correctionId, userId, pageable);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<CorrectionCommentDeleteResponseDTO> deleteComment(Long correctionId, Long commentId) {
        Long userId = SecurityUtil.getCurrentMemberId();
        CorrectionCommentDeleteResponseDTO response = correctionCommentService.deleteComment(commentId, userId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }
}





