package org.lxdproject.lxd.diarycommentlike.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.diarycommentlike.dto.DiaryCommentLikeResponseDTO;
import org.lxdproject.lxd.diarycommentlike.service.DiaryCommentLikeService;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


