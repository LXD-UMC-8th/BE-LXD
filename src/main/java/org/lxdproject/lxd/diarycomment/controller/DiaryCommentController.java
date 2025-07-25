package org.lxdproject.lxd.diarycomment.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diarycomment.dto.*;
import org.lxdproject.lxd.diarycomment.service.DiaryCommentService;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiaryCommentController implements DiaryCommentApi {

    private final DiaryCommentService diaryCommentService;

    @Override
    public ApiResponse<DiaryCommentResponseDTO> writeComment(
            Long diaryId,
            DiaryCommentRequestDTO request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        DiaryCommentResponseDTO response = diaryCommentService.writeComment(memberId, diaryId, request);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<DiaryCommentResponseDTO.CommentList> getComments(
            Long diaryId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        DiaryCommentResponseDTO.CommentList response =
                diaryCommentService.getComments(diaryId, pageable);
        return ApiResponse.of(SuccessStatus._OK, response);
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







