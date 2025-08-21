package org.lxdproject.lxd.correction.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.correction.dto.CorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.CorrectionResponseDTO;
import org.lxdproject.lxd.correction.service.CorrectionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
public class CorrectionController implements CorrectionApi {

    private final CorrectionService correctionService;

    @Override
    public ApiResponse<CorrectionResponseDTO.DiaryCorrectionsResponseDTO> getDiaryCorrections(
            Long diaryId, int page, int size) {
        return ApiResponse.onSuccess(
                correctionService.getCorrectionsByDiaryId(diaryId, page - 1, size)
        );
    }

    @Override
    public ApiResponse<CorrectionResponseDTO.CorrectionLikeResponseDTO> updateCorrectionLike(
            Long correctionId) {
        return ApiResponse.onSuccess(correctionService.toggleLikeCorrection(correctionId));
    }

    @Override
    public ApiResponse<CorrectionResponseDTO.CorrectionDetailDTO> createCorrection(
            CorrectionRequestDTO.CreateRequestDTO requestDto
    ) {
        return ApiResponse.onSuccess(correctionService.createCorrection(requestDto));
    }

    @Override
    public ApiResponse<CorrectionResponseDTO.ProvidedCorrectionsResponseDTO> getMyProvidedCorrections(
           int page, int size
    ) {
        return ApiResponse.onSuccess(correctionService.getMyProvidedCorrections(page- 1, size));
    }
}