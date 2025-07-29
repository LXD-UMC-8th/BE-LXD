package org.lxdproject.lxd.member.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.dto.*;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.service.FriendService;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ApiResponse<FriendMessageResponseDTO> sendFriendRequest(@RequestBody FriendRequestCreateRequestDTO requestDto) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        friendService.sendFriendRequest(currentMemberId, requestDto);
        return ApiResponse.onSuccess(new FriendMessageResponseDTO("요청이 전송되었습니다."));
    }

    @Override
    public ApiResponse<FriendMessageResponseDTO> acceptFriendRequest(@RequestBody FriendRequestAcceptRequestDTO requestDto) {
        Long receiverId = SecurityUtil.getCurrentMemberId();
        friendService.acceptFriendRequest(receiverId, requestDto);
        return ApiResponse.onSuccess(new FriendMessageResponseDTO("친구 요청을 수락했습니다."));
    }

    @Override
    public ApiResponse<FriendMessageResponseDTO> deleteFriend(@PathVariable Long friendId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        friendService.deleteFriend(currentMemberId, friendId);
        return ApiResponse.onSuccess(new FriendMessageResponseDTO("친구가 삭제되었습니다."));
    }

    @Override
    public ApiResponse<FriendRequestListResponseDTO> getFriendRequestList() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        FriendRequestListResponseDTO response = friendService.getPendingFriendRequests(currentMemberId);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<FriendDetailResponseDTO> getFriendDetail(@PathVariable Long friendId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        FriendDetailResponseDTO response = friendService.getFriendDetail(currentMemberId, friendId);
        return ApiResponse.onSuccess(response);
    }
}