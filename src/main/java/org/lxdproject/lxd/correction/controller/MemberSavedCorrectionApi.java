package org.lxdproject.lxd.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionResponseDTO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member Saved Correction API", description = "표현 학습(사용자 저장 교정) 관련 API입니다.")
@RequestMapping("/saved-corrections")
public interface MemberSavedCorrectionApi {

    @Operation(
            summary = "저장된 교정 내 메모 작성",
            description = "저장한 교정에 메모를 작성합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "메모 등록 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 메모가 존재함 또는 유효성 오류")
            }
    )
    @PostMapping("/memo")
    ApiResponse<MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO> createMemo(
            @RequestBody @Valid MemberSavedCorrectionRequestDTO.MemoRequestDTO request
    );

    @Operation(
            summary = "저장된 교정 내 메모 수정",
            description = "저장한 교정에 작성한 메모를 수정합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메모 수정 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 저장 교정 ID")
            }
    )
    @PatchMapping("/memo")
    ApiResponse<MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO> updateMemo(
            @RequestBody @Valid MemberSavedCorrectionRequestDTO.MemoRequestDTO request
    );

    @Operation(
            summary = "저장된 교정 내 메모 삭제",
            description = "저장한 교정에 작성한 메모를 삭제합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메모 삭제 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 저장 교정 ID")
            }
    )
    @DeleteMapping("/{correctionId}/memo")
    ApiResponse<MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO> deleteMemo(@PathVariable Long correctionId);
}