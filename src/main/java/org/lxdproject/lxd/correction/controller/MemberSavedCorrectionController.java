package org.lxdproject.lxd.correction.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionResponseDTO;
import org.lxdproject.lxd.correction.service.MemberSavedCorrectionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberSavedCorrectionController implements MemberSavedCorrectionApi {

    private final MemberSavedCorrectionService memberSavedCorrectionService;

    @Override
    public ApiResponse<MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO> createMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        return  ApiResponse.onSuccess(memberSavedCorrectionService.createMemo(request));
    }

    @Override
    public ApiResponse<MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO> updateMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        return  ApiResponse.onSuccess(memberSavedCorrectionService.updateMemo(request));
    }

    @Override
    public ApiResponse<MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO> deleteMemo(Long correctionId) {
        return ApiResponse.onSuccess(memberSavedCorrectionService.deleteMemo(correctionId));
    }
}
