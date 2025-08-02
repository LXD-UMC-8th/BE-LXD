package org.lxdproject.lxd.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.member.dto.LanguageChangeResponseDTO;
import org.lxdproject.lxd.member.dto.LanguageSettingRequestDTO;
import org.lxdproject.lxd.member.dto.LanguageSettingResponseDTO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member Language API", description = "회원 언어 설정 관련 API")
@RequestMapping("/members")
public interface LanguageSettingApi {

    @Operation(summary = "회원 언어 전체 조회 API(setting)", description = "현재 로그인한 회원의 Native/Study/System 언어를 조회합니다.")
    @GetMapping("/language")
    ApiResponse<LanguageSettingResponseDTO> getLanguageSetting();

    @Operation(summary = "회원 시스템 언어 변경 API", description = "현재 로그인한 회원의 System Language만 수정합니다.")
    @PatchMapping("/language")
    ApiResponse<LanguageChangeResponseDTO> setLanguageSetting(@RequestBody LanguageSettingRequestDTO languageSetting);
}

