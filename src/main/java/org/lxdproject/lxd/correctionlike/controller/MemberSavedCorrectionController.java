package org.lxdproject.lxd.correctionlike.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.correctionlike.dto.MemberSavedCorrectionRequestDTO;
import org.lxdproject.lxd.correctionlike.dto.MemberSavedCorrectionResponseDTO;
import org.lxdproject.lxd.correctionlike.service.MemberSavedCorrectionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberSavedCorrectionController implements MemberSavedCorrectionApi {

    private final MemberSavedCorrectionService memberSavedCorrectionService;

    @Override
    public ApiResponse<MemberSavedCorrectionResponseDTO.SavedListResponseDTO> getSavedCorrections(int page, int size) {
        return ApiResponse.onSuccess(memberSavedCorrectionService.getMySavedCorrections(page - 1, size));
    }

    @Override
    public ApiResponse<MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO> createMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        return ApiResponse.onSuccess(memberSavedCorrectionService.createMemo(request));
    }

    @Override
    public ApiResponse<MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO> updateMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        return ApiResponse.onSuccess(memberSavedCorrectionService.updateMemo(request));
    }

    @Override
    public ApiResponse<MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO> deleteMemo(Long savedCorrectionId) {
        return ApiResponse.onSuccess(memberSavedCorrectionService.deleteMemo(savedCorrectionId));
    }
}
