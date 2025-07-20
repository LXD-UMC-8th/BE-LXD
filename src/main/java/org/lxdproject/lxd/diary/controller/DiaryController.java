package org.lxdproject.lxd.diary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.ImageResponseDto;
import org.lxdproject.lxd.common.entity.enums.ImageDir;
import org.lxdproject.lxd.common.service.ImageService;
import org.lxdproject.lxd.diary.dto.DiaryDetailResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryRequestDTO;
import org.lxdproject.lxd.diary.service.DiaryService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class DiaryController implements DiaryApi{
    private final DiaryService diaryService;
    private final ImageService imageService;

    @Override
    public ApiResponse<DiaryDetailResponseDTO> createDiary(@Valid @RequestBody DiaryRequestDTO request) {
        DiaryDetailResponseDTO response = diaryService.createDiary(request);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<DiaryDetailResponseDTO> getDiaryDetail(@PathVariable Long id) {
        return ApiResponse.onSuccess(diaryService.getDiaryDetail(id));
    }

    @Override
    public ApiResponse<Boolean> deleteDiary(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        return ApiResponse.onSuccess(Boolean.TRUE);
    }

    @Override
    public ApiResponse<ImageResponseDto> uploadDiaryImage(@RequestPart("image") MultipartFile image) {
        ImageResponseDto response = imageService.uploadImage(image, ImageDir.DIARY);
        return ApiResponse.onSuccess(response);
    }
}
