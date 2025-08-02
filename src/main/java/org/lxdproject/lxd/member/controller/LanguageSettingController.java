package org.lxdproject.lxd.member.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.dto.LanguageChangeResponseDTO;
import org.lxdproject.lxd.member.dto.LanguageSettingRequestDTO;
import org.lxdproject.lxd.member.dto.LanguageSettingResponseDTO;
import org.lxdproject.lxd.member.service.LanguageSettingService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LanguageSettingController implements LanguageSettingApi {

    private final LanguageSettingService languageSettingService;

    @Override
    public ApiResponse<LanguageSettingResponseDTO> getLanguageSetting() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LanguageSettingResponseDTO response = languageSettingService.getLanguageSetting(memberId);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<LanguageChangeResponseDTO> setLanguageSetting(@RequestBody LanguageSettingRequestDTO request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LanguageChangeResponseDTO response = languageSettingService.setLanguage(memberId, request);
        return ApiResponse.of(SuccessStatus._OK, response);
    }
}

