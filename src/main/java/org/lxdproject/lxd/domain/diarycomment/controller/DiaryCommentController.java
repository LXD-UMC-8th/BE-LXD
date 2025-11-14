package org.lxdproject.lxd.domain.diarycomment.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.diarycomment.dto.DiaryCommentDeleteResponseDTO;
import org.lxdproject.lxd.domain.diarycomment.dto.DiaryCommentRequestDTO;
import org.lxdproject.lxd.domain.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.global.apiPayload.ApiResponse;
import org.lxdproject.lxd.global.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.global.common.dto.PageDTO;
import org.lxdproject.lxd.domain.diarycomment.service.DiaryCommentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class DiaryCommentController implements DiaryCommentApi {

    private final DiaryCommentService diaryCommentService;

    @Override
    public ApiResponse<DiaryCommentResponseDTO> writeComment(
            Long diaryId,
            DiaryCommentRequestDTO request
    ) {
        DiaryCommentResponseDTO response = diaryCommentService.writeComment(diaryId, request);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<PageDTO<DiaryCommentResponseDTO.Comment>> getComments(
            Long diaryId, int page, int size
    ) {
        PageDTO<DiaryCommentResponseDTO.Comment> dto = diaryCommentService.getComments(diaryId, page - 1, size);
        return ApiResponse.of(SuccessStatus._OK, dto);
    }

    @Override
    public ApiResponse<DiaryCommentDeleteResponseDTO> deleteComment(
            @PathVariable Long diaryId,
            @PathVariable Long commentId
    ) {
        DiaryCommentDeleteResponseDTO response = diaryCommentService.deleteComment(diaryId, commentId);
        return ApiResponse.of(SuccessStatus._OK, response);
    }


}







