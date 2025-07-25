package org.lxdproject.lxd.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionResponseDTO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member Saved Correction API", description = "표현 학습(사용자 저장 교정) 관련 API입니다.")
@RequestMapping("/corrections/saved")
public interface MemberSavedCorrectionApi {

    @Operation(
            summary = "저장한 교정 목록 조회",
            description = "좋아요 누른 교정 목록을 조회합니다",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리스트 조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            }
    )
    @GetMapping("")
    ApiResponse<MemberSavedCorrectionResponseDTO.SavedListResponseDTO> getSavedCorrections(
            @Parameter(description = "조회할 페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지에 포함할 교정 개수", example = "10") @RequestParam(defaultValue = "10") int size
    );


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
    @DeleteMapping("/{savedCorrectionId}/memo")
    ApiResponse<MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO> deleteMemo(@PathVariable Long savedCorrectionId);


}