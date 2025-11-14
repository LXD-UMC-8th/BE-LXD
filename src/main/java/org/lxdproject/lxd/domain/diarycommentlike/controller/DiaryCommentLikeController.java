package org.lxdproject.lxd.domain.diarycommentlike.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.apiPayload.ApiResponse;
import org.lxdproject.lxd.global.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.domain.diarycommentlike.dto.DiaryCommentLikeResponseDTO;
import org.lxdproject.lxd.domain.diarycommentlike.service.DiaryCommentLikeService;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiaryCommentLikeController implements DiaryCommentLikeApi {

    private final DiaryCommentLikeService diaryCommentLikeService;

    @Override
    public ApiResponse<DiaryCommentLikeResponseDTO> toggleCommentLike(Long commentId) {
        DiaryCommentLikeResponseDTO result = diaryCommentLikeService.toggleLike(commentId);
        return ApiResponse.onSuccess(SuccessStatus._OK, result);
    }
}


