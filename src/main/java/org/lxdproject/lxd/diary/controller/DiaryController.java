package org.lxdproject.lxd.diary.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.diary.dto.DiaryRequestDTO;
import org.lxdproject.lxd.diary.dto.DiaryResponseDTO;
import org.lxdproject.lxd.diary.service.DiaryService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiaryController implements DiaryApi{
    private final DiaryService diaryService;

    @Override
    public ApiResponse<DiaryResponseDTO> createDiary(@RequestBody DiaryRequestDTO request) {
        DiaryResponseDTO response = diaryService.createDiary(request);
        return ApiResponse.onSuccess(response);
    }
}
