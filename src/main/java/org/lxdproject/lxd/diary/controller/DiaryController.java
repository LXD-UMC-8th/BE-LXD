package org.lxdproject.lxd.diary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.annotation.CurrentMember;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.ImageDTO;
import org.lxdproject.lxd.common.dto.PageDTO;
import org.lxdproject.lxd.common.entity.enums.ImageDir;
import org.lxdproject.lxd.common.service.ImageService;
import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.service.DiaryService;
import org.lxdproject.lxd.diary.service.QuestionService;
import org.lxdproject.lxd.infra.storage.S3FileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.lxdproject.lxd.member.entity.Member;

@RestController
@RequiredArgsConstructor
@Validated
public class DiaryController implements DiaryApi{
    private final DiaryService diaryService;
    private final ImageService imageService;
    private final QuestionService questionService;
    private final S3FileService s3FileService;

    @Override
    public ApiResponse<DiaryDetailResponseDTO> createDiary(
            @CurrentMember Member member,
            @Valid @RequestBody DiaryRequestDTO request
    ) {
        DiaryDetailResponseDTO response = diaryService.createDiary(member, request);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<DiaryDetailResponseDTO> getDiaryDetail(
            @CurrentMember Member member,
            @PathVariable("diaryId") Long diaryId
    ) {
        return ApiResponse.onSuccess(diaryService.getDiaryDetail(member, diaryId));
    }

    @Override
    public ApiResponse<Boolean> deleteDiary(
            @CurrentMember Member member,
            @PathVariable("diaryId") Long diaryId
    ) {
        diaryService.deleteDiary(member, diaryId);
        return ApiResponse.onSuccess(Boolean.TRUE);
    }

    @Override
    public ApiResponse<ImageDTO> uploadDiaryImage(@RequestPart("image") MultipartFile image) {
        ImageDTO response = imageService.uploadImage(image, ImageDir.DIARY);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<String> deleteDiaryImage(@RequestBody ImageDTO imageDTO) {
        s3FileService.deleteImage(imageDTO.getImageUrl());
        return ApiResponse.onSuccess("요청한 이미지가 삭제되었습니다.");
    }

    @Override
    public ApiResponse<QuestionResponseDTO> getRandomQuestion(Language language) {
        QuestionResponseDTO response = questionService.getRandomQuestion(language);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<PageDTO<MyDiarySummaryResponseDTO>> getMyDiaries(
            @CurrentMember Member member,
            int page, int size, Boolean likedOnly
    ) {
        return ApiResponse.onSuccess(diaryService.getMyDiaries(member, likedOnly, page - 1, size));
    }

    @Override
    public ApiResponse<DiaryDetailResponseDTO> updateDiary(
            @CurrentMember Member member,
            @PathVariable("diaryId") Long diaryId,
            @Valid @RequestBody DiaryUpdateDTO request
    ) {
        DiaryDetailResponseDTO response = diaryService.updateDiary(member, diaryId, request);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<List<DiaryStatsResponseDTO>> getDiaryStats(
            @CurrentMember Member member,
            int year, int month
    ) {
        return ApiResponse.onSuccess(diaryService.getDiaryStats(member, year, month));
    }

    @Override
    public ApiResponse<PageDTO<DiarySummaryResponseDTO>> getFriendDiaries(
            @CurrentMember Member member,
            int page, int size
    ) {
        PageDTO<DiarySummaryResponseDTO> result = diaryService.getFriendDiaries(member, page - 1, size);
        return ApiResponse.onSuccess(result);
    }

    @Override
    public ApiResponse<PageDTO<DiarySummaryResponseDTO>> getLikedDiaries(
            @CurrentMember Member member,
            int page, int size
    ) {
        PageDTO<DiarySummaryResponseDTO> result = diaryService.getLikedDiaries(member, page - 1, size);
        return ApiResponse.onSuccess(result);
    }

    @Override
    public ApiResponse<PageDTO<DiarySummaryResponseDTO>> getExploreDiaries(
            @CurrentMember Member member,
            int page, int size, Language language
    ) {
        PageDTO<DiarySummaryResponseDTO> result = diaryService.getExploreDiaries(member, page - 1, size, language);
        return ApiResponse.onSuccess(result);
    }

    @Override
    public ApiResponse<MemberDiarySummaryResponseDTO> getMyDiarySummary(
            @CurrentMember Member member
    ) {
        MemberDiarySummaryResponseDTO response =
                diaryService.getDiarySummary(member.getId(), member.getId(), false);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<MemberDiarySummaryResponseDTO> getUserDiarySummary(
            @CurrentMember Member member,
            @PathVariable("memberId") Long memberId
    ) {
        MemberDiarySummaryResponseDTO response =
                diaryService.getDiarySummary(memberId, member.getId(), true);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<PageDTO<MyDiarySummaryResponseDTO>> getDiariesByMemberId(
            @CurrentMember Member viewer,
            @PathVariable("memberId") Long memberId,
            int page, int size
    ) {
        PageDTO<MyDiarySummaryResponseDTO> result =
                diaryService.getDiariesByMemberId(viewer, memberId, page - 1, size);
        return ApiResponse.onSuccess(result);
    }
}
