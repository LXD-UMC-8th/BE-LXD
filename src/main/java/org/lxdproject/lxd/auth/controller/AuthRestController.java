package org.lxdproject.lxd.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.auth.dto.AuthRequestDTO;
import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.auth.service.AuthService;
import org.lxdproject.lxd.member.dto.MemberRequestDTO;
import org.lxdproject.lxd.member.dto.MemberResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
public class AuthRestController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 통해 로그인하고 JWT 토큰을 발급받습니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 실패 또는 파라미터 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일/비밀번호 불일치 또는 토큰이 올바르지 않음")
    })
    public ApiResponse<AuthResponseDTO.LoginResponseDTO> login(@RequestBody @Valid AuthRequestDTO.LoginRequestDTO loginRequestDTO) {

        AuthResponseDTO.LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
        return ApiResponse.onSuccess(loginResponseDTO);
    }

    @PostMapping("/emails/verification-requests")
    @Operation(summary = "이메일 인증 링크 발송 API", description = "해당 이메일로 이메일 인증 링크를 발송합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 실패 또는 파라미터 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 오류 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 문제")

    })
    public ApiResponse<String> sendVerificationEmail(@RequestBody @Valid AuthRequestDTO.sendVerificationRequestDTO sendVerificationRequestDTO) {

        authService.sendVerificationEmail(sendVerificationRequestDTO);
        return ApiResponse.onSuccess("입력한 이메일로 인증 링크를 전송했습니다.");
    }


}
