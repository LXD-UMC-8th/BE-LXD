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

    @GetMapping("/profile")
    @Operation(summary = "프로필 조회 api", description = "프로필 수정 화면에서 프로필을 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 형식 또는 유효성 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
    })
    public ApiResponse<MemberResponseDTO.MemberInfoDTO> getProfileInfo() {
        return ApiResponse.onSuccess(memberService.getMemberInfo());
    }

}
