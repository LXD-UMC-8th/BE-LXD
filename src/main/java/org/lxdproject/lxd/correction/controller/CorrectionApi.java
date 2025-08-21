package org.lxdproject.lxd.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.correction.dto.CorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.CorrectionResponseDTO;
import org.lxdproject.lxd.validation.annotation.PageSizeValid;
import org.lxdproject.lxd.validation.annotation.PageValid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Correction API", description = "교정 관련 API입니다.")
@RequestMapping("/corrections")
public interface CorrectionApi {

    @GetMapping("/diary/{diaryId}")
    @Operation(
            summary = "특정 일기 교정 조회 API",
            description = "특정 일기에 작성된 교정 리스트를 최신순으로 조회합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID")
            }
    )
    ApiResponse<CorrectionResponseDTO.DiaryCorrectionsResponseDTO> getDiaryCorrections(
            @PathVariable Long diaryId,
            @Parameter(description = "조회할 페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") @PageValid int page,
            @Parameter(description = "한 페이지에 포함할 교정 개수", example = "10") @RequestParam(defaultValue = "10") @PageSizeValid int size
    );

    @Operation(
            summary = "교정 작성 API",
            description = "일기의 문장에서 특정 부분을 교정하고 피드백 코멘트를 작성하여 등록합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "교정 등록 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 형식 또는 유효성 실패"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID")
            }
    )
    @PostMapping
    ApiResponse<CorrectionResponseDTO.CorrectionDetailDTO> createCorrection(
            @RequestBody @Valid CorrectionRequestDTO.CreateRequestDTO requestDto
    );

    @Operation(
            summary = "교정 저장(좋아요) API ",
            description = "일기 내 교정 중 좋아요를 눌러 저장합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "좋아요 등록 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 교정 ID")
            }
    )
    @PostMapping("/{correctionId}/likes")
    ApiResponse<CorrectionResponseDTO.CorrectionLikeResponseDTO> updateCorrectionLike(
            @PathVariable Long correctionId
    );

    @Operation(
            summary = "나의 작성 교정 조회 API",
            description = "현재 로그인한 사용자가 다른 사람의 일기에 작성한 교정 리스트를 반환합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/provided")
    ApiResponse<CorrectionResponseDTO.ProvidedCorrectionsResponseDTO> getMyProvidedCorrections(

            @Parameter(description = "조회할 페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") @PageValid int page,

            @Parameter(description = "한 페이지에 포함할 교정 개수", example = "10")
            @RequestParam(defaultValue = "10") @PageSizeValid int size
    );
}