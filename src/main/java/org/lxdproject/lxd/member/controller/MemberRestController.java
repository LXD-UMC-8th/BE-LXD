package org.lxdproject.lxd.member.controller;

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
@RequestMapping("/members")
@Validated
public class MemberRestController {

    private final MemberService memberService;

    @GetMapping("/join")
    public ApiResponse<MemberResponseDTO.JoinResponseDTO> join(@RequestBody @Valid MemberRequestDTO.JoinRequestDTO joinRequestDTO) {

        Member member = memberService.join(joinRequestDTO);
        return ApiResponse.onSuccess(MemberConverter.toJoinResponseDTO(member));
    }

}
