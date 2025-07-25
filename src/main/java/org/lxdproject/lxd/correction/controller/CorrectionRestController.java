package org.lxdproject.lxd.correction.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.correction.dto.CorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.CorrectionResponseDTO;
import org.lxdproject.lxd.correction.service.CorrectionService;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CorrectionRestController implements CorrectionApi {

    private final CorrectionService correctionService;

    @Override
    public ApiResponse<CorrectionResponseDTO.DiaryCorrectionsResponseDTO> getDiaryCorrections(
            Long diaryId, int page, int size, Member member) {
        return ApiResponse.onSuccess(correctionService.getCorrectionsByDiaryId(diaryId, page, size, member));
    }

    @Override
    public ApiResponse<CorrectionResponseDTO.CorrectionDetailDTO> createCorrection(
            CorrectionRequestDTO.CreateRequestDTO requestDto,
            Member member
    ) {
        return ApiResponse.onSuccess(correctionService.createCorrection(requestDto, member));
    }

    @Override
    public ApiResponse<CorrectionResponseDTO.ProvidedCorrectionsResponseDTO> getMyProvidedCorrections(
            Member member, int page, int size
    ) {
        return ApiResponse.onSuccess(correctionService.getMyProvidedCorrections(member, page, size));
    }
}