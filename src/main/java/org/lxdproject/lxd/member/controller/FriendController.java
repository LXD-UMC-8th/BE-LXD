package org.lxdproject.lxd.member.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.dto.FriendListResponseDTO;
import org.lxdproject.lxd.member.service.FriendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public ApiResponse<FriendListResponseDTO> getFriendList() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        FriendListResponseDTO response = friendService.getFriendList(currentMemberId);
        return ApiResponse.onSuccess(response);
    }
}
