package org.lxdproject.lxd.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.auth.dto.AuthRequestDTO;
import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.auth.dto.oauth.GoogleUserInfo;
import org.lxdproject.lxd.auth.service.AuthService;
import org.lxdproject.lxd.auth.service.oauth.GoogleOAuthClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final GoogleOAuthClient googleOAuthClient;

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

    @GetMapping("/emails/verifications")
    @Operation(summary = "이메일 인증 API", description = "이메일 인증 후 프론트엔드 페이지로 리다이렉트 합니다", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "리다이렉트 오류")
    })
    public void verifyEmailTokenAndRedirect(@RequestParam("token") String token, HttpServletResponse response) {
        authService.verifyEmailTokenAndRedirect(token, response);
    }

    @PostMapping("/google/login")
    @Operation(summary = "구글 로그인 API", description = "구글 로그인 후 발급받은 code로 소셜 로그인 또는 회원가입을 진행합니다", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 실패 또는 파라미터 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "구글 accessToken 요청 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "사용자 정보 요청 실패"),


    })
    public ApiResponse<AuthResponseDTO.SocialLoginResponseDTO>loginWithGoogle(@RequestBody AuthRequestDTO.SocialLoginRequestDTO SocialLoginRequestDTO) {

        String accessToken = googleOAuthClient.requestAccessToken(SocialLoginRequestDTO.getCode());
        GoogleUserInfo googleUserInfo = googleOAuthClient.requestUserInfo(accessToken);

        AuthResponseDTO.SocialLoginResponseDTO socialLoginResponseDTO = authService.socialLogin(googleUserInfo);

        return ApiResponse.onSuccess(socialLoginResponseDTO);
    }
}
