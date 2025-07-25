package org.lxdproject.lxd.diary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.ImageResponseDTO;
import org.lxdproject.lxd.common.entity.enums.ImageDir;
import org.lxdproject.lxd.common.service.ImageService;
import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.service.DiaryService;
import org.lxdproject.lxd.diary.service.QuestionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiaryController implements DiaryApi{
    private final DiaryService diaryService;
    private final ImageService imageService;
    private final QuestionService questionService;

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
    public ApiResponse<ImageResponseDTO> uploadDiaryImage(@RequestPart("image") MultipartFile image) {
        ImageResponseDTO response = imageService.uploadImage(image, ImageDir.DIARY);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<QuestionResponseDTO> getRandomQuestion(Language language) {
        QuestionResponseDTO response = questionService.getRandomQuestion(language);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<DiarySliceResponseDTO> getMyDiaries(int page, int size, Boolean likedOnly) {
        return ApiResponse.onSuccess(diaryService.getMyDiaries(page, size, likedOnly));
    }


    @Override
    public ApiResponse<DiaryDetailResponseDTO> updateDiary(Long id, @Valid @RequestBody DiaryRequestDTO request) {
        DiaryDetailResponseDTO response = diaryService.updateDiary(id, request);
        return ApiResponse.onSuccess(response);
    }

//    @Override
//    public ApiResponse<DiarySliceResponseDto> getMyDiaries(int page, int size, Boolean likedOnly) {
//        return ApiResponse.onSuccess(diaryService.getMyDiaries(page, size, likedOnly));
//    }


    @Override
    public ApiResponse<List<DiaryStatsResponseDTO>> getDiaryStats(int year, int month) {
        return ApiResponse.onSuccess(diaryService.getDiaryStats(year, month));
    }
}
