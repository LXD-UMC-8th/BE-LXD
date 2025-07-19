package org.lxdproject.lxd.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.correction.dto.CorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.CorrectionResponseDTO;
import org.lxdproject.lxd.correction.service.CorrectionService;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/corrections")
@Validated
public class CorrectionRestController {
    private final CorrectionService correctionService;

    @PostMapping
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
    public ApiResponse<CorrectionResponseDTO.CreateResponseDTO> createCorrection(
            @RequestBody @Valid CorrectionRequestDTO.CreateRequestDTO requestDto,
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponse.onSuccess( correctionService.createCorrection(requestDto, member));
    }
}
