package org.lxdproject.lxd.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.member.converter.MemberConverter;
import org.lxdproject.lxd.member.dto.MemberRequestDTO;
import org.lxdproject.lxd.member.dto.MemberResponseDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.service.MemberService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Validated
public class MemberRestController {

    private final MemberService memberService;

    @PostMapping("/join")
    @Operation(summary = "회원가입 api", description = "계정 생성", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 실패, 파라미터 오류 등"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일, 닉네임 중복")
    })
    public ApiResponse<MemberResponseDTO.JoinResponseDTO> join(@RequestBody @Valid MemberRequestDTO.JoinRequestDTO joinRequestDTO) {

        Member member = memberService.join(joinRequestDTO);
        return ApiResponse.onSuccess(MemberConverter.toJoinResponseDTO(member));
    }

    @PatchMapping("/language")
    @Operation(summary = "언어 변경 API", description = "회원의 학습 언어를 변경합니다.")
    public ApiResponse<MemberResponseDTO.UpdateLanguageResponseDTO> updateLanguage(
            @RequestBody @Valid MemberRequestDTO.UpdateLanguageRequestDTO requestDTO
            /* , @AuthenticationPrincipal CustomUserDetails userDetails */
    ) {
        Long memberId = 1L; // 🧪 테스트용 — 추후 로그인 연동되면 교체
        return ApiResponse.onSuccess(memberService.updateLanguage(memberId, requestDTO.getLanguage()));
    }


}
