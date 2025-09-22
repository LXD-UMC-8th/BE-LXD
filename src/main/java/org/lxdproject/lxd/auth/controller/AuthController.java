package org.lxdproject.lxd.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.auth.dto.AuthRequestDTO;
import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.auth.dto.oauth.GoogleUserInfo;
import org.lxdproject.lxd.auth.service.AuthService;
import org.lxdproject.lxd.auth.service.oauthClient.GoogleOAuthClient;
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

        authService.validateSendVerificationRequestDTOOrThrow(sendVerificationRequestDTO);
        authService.sendVerificationEmail(sendVerificationRequestDTO);
        return ApiResponse.onSuccess("입력한 이메일로 인증 링크를 전송했습니다.");
    }

    @Override
    public void verifyEmailTokenAndRedirect(@RequestParam("token") String token, HttpServletResponse response) {
        authService.verifyEmailTokenAndRedirect(token, response);
    }

    @Override
    public ApiResponse<AuthResponseDTO.SocialLoginResponseDTO>loginWithGoogle(@RequestBody AuthRequestDTO.SocialLoginRequestDTO socialLoginRequestDTO) {

        String accessToken = googleOAuthClient.requestAccessToken(socialLoginRequestDTO.getCode());
        GoogleUserInfo googleUserInfo = googleOAuthClient.requestUserInfo(accessToken);

        AuthResponseDTO.SocialLoginResponseDTO socialLoginResponseDTO = authService.socialLogin(googleUserInfo);

        return ApiResponse.onSuccess(socialLoginResponseDTO);
    }

    @Override
    public ApiResponse<AuthResponseDTO.GetEmailByTokenResponseDTO> getEmailByToken(String token) {

        AuthResponseDTO.GetEmailByTokenResponseDTO getEmailByTokenResponseDTO = authService.getEmailByToken(token);

        return ApiResponse.onSuccess(getEmailByTokenResponseDTO);
    }

    @Override
    public ApiResponse<AuthResponseDTO.ReissueResponseDTO> reissue(@RequestBody @Valid AuthRequestDTO.ReissueRequestDTO reissueRequestDTO) {

        AuthResponseDTO.ReissueResponseDTO reissueResponseDTO = authService.reissue(reissueRequestDTO);

        return ApiResponse.onSuccess(reissueResponseDTO);
    }

    @Override
    public ApiResponse<String> logout(@RequestBody AuthRequestDTO.LogoutRequestDTO logoutRequestDTO) {

        authService.logout(logoutRequestDTO);

        return ApiResponse.onSuccess("로그아웃에 성공했습니다");

    }

    @Override
    public ApiResponse<String> recover() {

        authService.recover();

        return ApiResponse.onSuccess("계정이 복구 되었습니다. 재로그인 해주세요");
    }


}
