package org.lxdproject.lxd.member.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.dto.FriendListResponseDTO;
import org.lxdproject.lxd.member.dto.FriendRequestCreateRequestDTO;
import org.lxdproject.lxd.member.dto.FriendRequestCreateResponseDTO;
import org.lxdproject.lxd.member.service.FriendService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class FriendController implements FriendApi {

    private final FriendService friendService;

    @Override
    public ApiResponse<FriendListResponseDTO> getFriendList() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        FriendListResponseDTO response = friendService.getFriendList(currentMemberId);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<FriendRequestCreateResponseDTO> sendFriendRequest(@RequestBody FriendRequestCreateRequestDTO requestDto) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        friendService.sendFriendRequest(currentMemberId, requestDto);
        return ApiResponse.onSuccess(new FriendRequestCreateResponseDTO("요청이 전송되었습니다."));
    }
}