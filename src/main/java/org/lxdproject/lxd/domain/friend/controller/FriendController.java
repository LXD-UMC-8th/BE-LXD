package org.lxdproject.lxd.domain.friend.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.friend.dto.*;
import org.lxdproject.lxd.global.apiPayload.ApiResponse;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.FriendHandler;
import org.lxdproject.lxd.global.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.global.security.SecurityUtil;
import org.lxdproject.lxd.friend.dto.*;
import org.lxdproject.lxd.domain.friend.service.FriendService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Validated
public class FriendController implements FriendApi {

    private final FriendService friendService;

    @Override
    public ApiResponse<FriendListResponseDTO> getFriendList(int page, int size) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        FriendListResponseDTO response = friendService.getFriendList(currentMemberId, page, size);
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
    public ApiResponse<FriendRequestListResponseDTO> getFriendRequestList(int receivedPage, int sentPage, int size) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        FriendRequestListResponseDTO response = friendService.getPendingFriendRequests(currentMemberId, receivedPage, sentPage, size);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<FriendMessageResponseDTO> refuseFriendRequest(FriendRequestRefuseRequestDTO requestDto) {
        friendService.refuseFriendRequest(requestDto);
        return ApiResponse.onSuccess(new FriendMessageResponseDTO("친구 요청을 거절하였습니다."));
    }

    @Override
    public ApiResponse<FriendMessageResponseDTO> cancelFriendRequest(FriendRequestCancelRequestDTO requestDto) {
        friendService.cancelFriendRequest(requestDto);
        return ApiResponse.onSuccess(new FriendMessageResponseDTO("친구 요청을 취소하였습니다."));
    }

    @Override
    public ApiResponse<FriendSearchResponseDTO> searchFriends(String query, int page, int size) {
        if (query == null || query.trim().isEmpty()) {
            throw new FriendHandler(ErrorStatus.SEARCH_QUERY_REQUIRED);
        }
        if (query.trim().length() < 1) {
            throw new FriendHandler(ErrorStatus.SEARCH_QUERY_REQUIRED);
        }

        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.onSuccess(friendService.searchFriends(memberId, query, page, size));
    }

    @Override
    public ApiResponse<List<String>> getRecentFriendSearchKeywords(int limit) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        List<String> recentKeywords = friendService.getRecentSearchKeywords(memberId, limit);
        return ApiResponse.onSuccess(recentKeywords);
    }

    @Override
    public ApiResponse<String> deleteRecentFriendSearchKeyword(String query) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        friendService.deleteKeyword(memberId, query);
        return ApiResponse.onSuccess(query + " 검색 기록을 삭제하였습니다.");
    }

    @Override
    public ApiResponse<String> clearRecentFriendSearchKeywords() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        friendService.clearKeywords(memberId);
        return ApiResponse.onSuccess("검색 기록을 초기화 하였습니다.");
    }

}