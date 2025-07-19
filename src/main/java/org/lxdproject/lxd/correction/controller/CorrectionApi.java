package org.lxdproject.lxd.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.correction.dto.CorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.CorrectionResponseDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Correction API", description = "교정 관련 API입니다.")
@RequestMapping("/corrections")
public interface CorrectionApi {

    @Operation(
            summary = "교정 등록 API",
            description = "일기의 문장에서 특정 부분을 교정하고 피드백 코멘트를 작성하여 등록합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "교정 등록 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 형식 또는 유효성 실패"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID")
            }
    )
    @PostMapping
    ApiResponse<CorrectionResponseDTO.CreateResponseDTO> createCorrection(
            @RequestBody @Valid CorrectionRequestDTO.CreateRequestDTO requestDto,
            @AuthenticationPrincipal Member member
    );

    @Operation(
            summary = "내가 제공한 교정 목록 조회",
            description = "현재 로그인한 사용자가 다른 사람의 일기에 작성한 교정 리스트를 반환합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/provided")
    ApiResponse<CorrectionResponseDTO.ProvidedCorrectionsResponseDTO> getMyProvidedCorrections(
            @AuthenticationPrincipal Member member,

            @Parameter(description = "조회할 페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "한 페이지에 포함할 교정 개수", example = "10")
            @RequestParam(defaultValue = "10") int size
    );
}