package org.lxdproject.lxd.diarylike.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.diarylike.dto.DiaryLikeResponseDTO;
import org.lxdproject.lxd.diarylike.service.DiaryLikeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class DiaryLikeController implements DiaryLikeApi {

    private final DiaryLikeService diaryLikeService;

    @Override
    public ApiResponse<DiaryLikeResponseDTO.ToggleDiaryLikeResponseDTO> toggleDiaryLike(@PathVariable Long diaryId) {

        DiaryLikeResponseDTO.ToggleDiaryLikeResponseDTO toggleDiaryLikeResponseDTO = diaryLikeService.toggleDiaryLike(diaryId);
        return ApiResponse.onSuccess(toggleDiaryLikeResponseDTO);

    }

}
