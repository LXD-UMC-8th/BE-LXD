package org.lxdproject.lxd.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.member.dto.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Member API", description = "사용자 관련 API 입니다.")
@RequestMapping("/members")
public interface MemberApi {

    @PostMapping(value = "/join", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "회원가입 api", description = "계정 생성")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "파라미터 오류", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일, 닉네임 중복", content = @Content(schema = @Schema(implementation = ApiResponse.class))),

    })
    public ApiResponse<MemberResponseDTO.JoinResponseDTO> join(@RequestPart(value = "data") @Valid MemberRequestDTO.JoinRequestDTO joinRequestDTO, @RequestPart(required = false) MultipartFile profileImg);

    @GetMapping("/profile")
    @Operation(summary = "프로필 조회 api", description = "프로필 수정 화면에서 프로필을 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 형식 또는 유효성 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
    })
    ApiResponse<MemberResponseDTO.MemberInfoDTO> getProfileInfo();

    @GetMapping("/check-username")
    @Operation(summary = "아이디 중복 확인 api", description = "중복된 아이디인지 검사하는 api 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "중복 검사 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "파라미터 오류", content = @Content(schema = @Schema(implementation = ApiResponse.class))),

    })
    public ApiResponse<MemberResponseDTO.CheckUsernameResponseDTO> checkUsername(@RequestParam String username);

    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 수정", description = "닉네임과 프로필 이미지를 수정합니다.")
    public ApiResponse<MemberResponseDTO.MemberInfoDTO> updateProfileInfo(
            @RequestPart("data") String data,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) throws JsonProcessingException;

    @Operation(summary = "언어 조회 API", description = "로그인한 회원의 모국어, 학습언어, 시스템 언어를 조회합니다.")
    @GetMapping("/language")
    ApiResponse<LanguageSettingResponseDTO> getLanguageSetting();


    @Operation(summary = "시스템 언어 변경 API", description = "로그인한 회원의 시스템 언어를 수정합니다.")
    @PatchMapping("/system-language")
    ApiResponse<LanguageChangeResponseDTO> setLanguageSetting(@Valid @RequestBody LanguageSettingRequestDTO languageSetting);

    @Operation(summary = "비밀번호 변경 API", description = "이메일 인증이 완료된 계정의 비밀번호를 수정합니다.")
    @PatchMapping("/password")
    ApiResponse<String> setPasswordSetting(@Valid @RequestBody MemberRequestDTO.SetPasswordSettingRequestDTO setPasswordSettingRequestDTO);
}
