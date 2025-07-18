package org.lxdproject.lxd.diary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.diary.dto.DiaryDetailResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryRequestDTO;
import org.lxdproject.lxd.diary.dto.QuestionRequestDTO;
import org.lxdproject.lxd.diary.dto.QuestionResponseDTO;
import org.lxdproject.lxd.diary.service.DiaryService;
import org.lxdproject.lxd.diary.service.QuestionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiaryController implements DiaryApi{
    private final DiaryService diaryService;
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
    public ApiResponse<QuestionResponseDTO> getRandomQuestion(QuestionRequestDTO request) {
        QuestionResponseDTO response = questionService.getRandomQuestion(request);
        return ApiResponse.onSuccess(response);
    }
}
