package org.lxdproject.lxd.diary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.ImageResponseDTO;
import org.lxdproject.lxd.common.entity.enums.ImageDir;
import org.lxdproject.lxd.common.service.ImageService;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.service.DiaryService;
import org.lxdproject.lxd.diary.service.QuestionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
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
    public ApiResponse<DiaryDetailResponseDTO> getDiaryDetail(@PathVariable("diaryId") Long diaryId) {
        return ApiResponse.onSuccess(diaryService.getDiaryDetail(diaryId));
    }

    @Override
    public ApiResponse<Boolean> deleteDiary(@PathVariable("diaryId") Long diaryId) {
        diaryService.deleteDiary(diaryId);
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
    public ApiResponse<MyDiarySliceResponseDTO> getMyDiaries(int page, int size, Boolean likedOnly) {
        return ApiResponse.onSuccess(diaryService.getMyDiaries(page, size, likedOnly));
    }


    @Override
    public ApiResponse<DiaryDetailResponseDTO> updateDiary(
            @PathVariable("diaryId") Long diaryId,
            @Valid @RequestBody DiaryRequestDTO request
    ) {
        DiaryDetailResponseDTO response = diaryService.updateDiary(diaryId, request);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<List<DiaryStatsResponseDTO>> getDiaryStats(int year, int month) {
        return ApiResponse.onSuccess(diaryService.getDiaryStats(year, month));
    }

    @Override
    public ApiResponse<DiarySliceResponseDTO> getFriendDiaries(@RequestParam("page") int page,
                                                                 @RequestParam("size") int size) {
        Long currentUserId = SecurityUtil.getCurrentMemberId();
        Pageable pageable = PageRequest.of(page - 1, size);
        DiarySliceResponseDTO response = diaryService.getDiariesOfFriends(currentUserId, pageable);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<DiarySliceResponseDTO> getLikedDiaries(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        DiarySliceResponseDTO response = diaryService.getLikedDiaries(pageable);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<DiarySliceResponseDTO> getExploreDiaries(int page, int size, Language language) {
        Pageable pageable = PageRequest.of(page - 1, size);
        DiarySliceResponseDTO result = diaryService.getExploreDiaries(pageable, language);
        return ApiResponse.onSuccess(result);
    }

    @Override
    public ApiResponse<MemberDiarySummaryResponseDTO> getMyDiarySummary() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberDiarySummaryResponseDTO response = diaryService.getDiarySummary(currentMemberId, currentMemberId);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<MemberDiarySummaryResponseDTO> getUserDiarySummary(Long memberId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberDiarySummaryResponseDTO response = diaryService.getDiarySummary(memberId, currentMemberId);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<MyDiarySliceResponseDTO> getDiariesByMemberId(@PathVariable Long memberId,
                                                                     @RequestParam("page") int page,
                                                                     @RequestParam("size") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        MyDiarySliceResponseDTO response = diaryService.getDiariesByMemberId(memberId, pageable);
        return ApiResponse.onSuccess(response);
    }
}
