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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Validated
public class MemberController implements MemberApi {

    private final MemberService memberService;

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

}
