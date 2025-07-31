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

@RestController
@RequiredArgsConstructor
@Validated
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @Override
    public ApiResponse<MemberResponseDTO.JoinResponseDTO> join(@RequestBody @Valid MemberRequestDTO.JoinRequestDTO joinRequestDTO) {

        Member member = memberService.join(joinRequestDTO);
        return ApiResponse.onSuccess(MemberConverter.toJoinResponseDTO(member));
    }

    @Override
    public ApiResponse<MemberResponseDTO.MemberInfoDTO> getProfileInfo() {
        return ApiResponse.onSuccess(memberService.getMemberInfo());
    }
}
