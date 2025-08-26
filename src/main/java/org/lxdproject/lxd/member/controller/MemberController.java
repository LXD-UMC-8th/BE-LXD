package org.lxdproject.lxd.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.apiPayload.code.status.SuccessStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.converter.MemberConverter;
import org.lxdproject.lxd.member.dto.*;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.service.MemberService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Validated
public class MemberController implements MemberApi {

    private final MemberService memberService;
    private final ObjectMapper objectMapper;


    @Override
    public ApiResponse<MemberResponseDTO.JoinResponseDTO> join(@RequestPart(value = "data") @Valid MemberRequestDTO.JoinRequestDTO joinRequestDTO, @RequestPart(required = false) MultipartFile profileImg) {

        Member member = memberService.join(joinRequestDTO, profileImg);
        return ApiResponse.onSuccess(MemberConverter.toJoinResponseDTO(member));
    }

    @Override
    public ApiResponse<MemberResponseDTO.MemberInfoDTO> getProfileInfo() {
        return ApiResponse.onSuccess(memberService.getMemberInfo());
    }

    @Override
    public ApiResponse<MemberResponseDTO.CheckUsernameResponseDTO> checkUsername(@RequestParam String username) {
        return ApiResponse.onSuccess(memberService.isUsernameDuplicated(username));
    }

    @Override
    public ApiResponse<MemberResponseDTO.MemberInfoDTO> updateProfileInfo(
            @RequestPart("data") String data,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) {
        try {
            MemberRequestDTO.ProfileUpdateDTO dto = objectMapper.readValue(data, MemberRequestDTO.ProfileUpdateDTO.class);
            return ApiResponse.onSuccess(memberService.updateMemberInfo(dto, profileImg));
        } catch (JsonProcessingException e) {
            throw new MemberHandler(ErrorStatus.INVALID_PROFILE_DATA);
        }
    }

    @Override
    public ApiResponse<LanguageSettingResponseDTO> getLanguageSetting() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LanguageSettingResponseDTO response = memberService.getLanguageSetting(memberId);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<LanguageChangeResponseDTO> setLanguageSetting(@Valid @RequestBody LanguageSettingRequestDTO request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LanguageChangeResponseDTO response = memberService.setSystemLanguage(memberId, request);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

    @Override
    public ApiResponse<String> setPasswordSetting(@Valid @RequestBody MemberRequestDTO.SetPasswordSettingRequestDTO setPasswordSettingRequestDTO) {

        memberService.setPasswordSetting(setPasswordSettingRequestDTO);
        return ApiResponse.onSuccess("비밀번호가 수정됐습니다.");
    }

    @Override
    public ApiResponse<String> deleteProfileImage() {
        memberService.deleteProfileImage();
        return ApiResponse.onSuccess("프로필 이미지가 삭제되었습니다.");
    }
}
