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
@Validated
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final GoogleOAuthClient googleOAuthClient;

    @Override
    public ApiResponse<AuthResponseDTO.LoginResponseDTO> login(@RequestBody @Valid AuthRequestDTO.LoginRequestDTO loginRequestDTO) {

        AuthResponseDTO.LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
        return ApiResponse.onSuccess(loginResponseDTO);
    }

    @Override
    public ApiResponse<String> sendVerificationEmail(@RequestBody @Valid AuthRequestDTO.sendVerificationRequestDTO sendVerificationRequestDTO) {

        authService.sendVerificationEmail(sendVerificationRequestDTO);
        return ApiResponse.onSuccess("입력한 이메일로 인증 링크를 전송했습니다.");
    }

    @Override
    public void verifyEmailTokenAndRedirect(@RequestParam("token") String token, HttpServletResponse response) {
        authService.verifyEmailTokenAndRedirect(token, response);
    }

    @Override
    public ApiResponse<AuthResponseDTO.SocialLoginResponseDTO>loginWithGoogle(@RequestBody AuthRequestDTO.SocialLoginRequestDTO SocialLoginRequestDTO) {

        String accessToken = googleOAuthClient.requestAccessToken(SocialLoginRequestDTO.getCode());
        GoogleUserInfo googleUserInfo = googleOAuthClient.requestUserInfo(accessToken);

        AuthResponseDTO.SocialLoginResponseDTO socialLoginResponseDTO = authService.socialLogin(googleUserInfo);

        return ApiResponse.onSuccess(socialLoginResponseDTO);
    }
}
