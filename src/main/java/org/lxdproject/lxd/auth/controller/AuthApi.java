package org.lxdproject.lxd.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.auth.dto.AuthRequestDTO;
import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.auth.dto.oauth.GoogleUserInfo;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "인증 관련 API입니다.")
@RequestMapping("/auth")
public interface AuthApi {

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 통해 로그인하고 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "파라미터 오류", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class))),

    })
    ApiResponse<AuthResponseDTO.LoginResponseDTO> login(@RequestBody @Valid AuthRequestDTO.LoginRequestDTO loginRequestDTO);

    @PostMapping("/emails/verification-requests")
    @Operation(summary = "이메일 인증 링크 발송 API", description = "해당 이메일로 이메일 인증 링크를 발송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 링크 전송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 문제")
    })
    ApiResponse<String> sendVerificationEmail(@RequestBody @Valid AuthRequestDTO.sendVerificationRequestDTO sendVerificationRequestDTO);

    @GetMapping("/emails/verifications")
    @Operation(summary = "이메일 인증 API", description = "이메일 인증 후 프론트엔드 페이지로 리다이렉트 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 문제")
    })
    void verifyEmailTokenAndRedirect(@Parameter(description = "전달된 이메일에서 받은 토큰", example = "wknfklsdfjkd") @RequestParam("token") String token, HttpServletResponse response);

    @PostMapping("/google/login")
    @Operation(summary = "구글 로그인 API", description = "구글 로그인 후 발급받은 code로 소셜 로그인 또는 회원가입을 진행합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구글 로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 실패 또는 파라미터 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "사용자 정보 요청 실패"),
    })
    ApiResponse<AuthResponseDTO.SocialLoginResponseDTO> loginWithGoogle(@RequestBody AuthRequestDTO.SocialLoginRequestDTO socialLoginRequestDTO);

    @GetMapping("/email")
    @Operation(summary = "이메일 인증 시 프론트엔드 uri에 전달한 토큰의 주인(이메일)을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 실패 또는 파라미터 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 이메일 토큰"),
    })
    ApiResponse<AuthResponseDTO.GetEmailByTokenResponseDTO> getEmailByToken(
            @Parameter(description = "이메일 인증 토큰", required = true, example = "abc123token")
            @RequestParam("token") @NotBlank String token
    );

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급 API", description = "access token 및 refresh token 재발급 기능입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 오류 또는 토큰 만료시간 오버")
    })
    ApiResponse<AuthResponseDTO.ReissueResponseDTO> reissue(@RequestBody @Valid AuthRequestDTO.ReissueRequestDTO reissueRequestDTO);

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "리프레쉬 토큰을 redis에서 제거합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 오류 또는 토큰 만료시간 오버")
    })
    ApiResponse<String> logout(@RequestBody @Valid AuthRequestDTO.LogoutRequestDTO logoutRequestDTO);

}

