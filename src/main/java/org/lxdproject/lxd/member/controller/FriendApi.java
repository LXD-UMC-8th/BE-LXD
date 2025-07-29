package org.lxdproject.lxd.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.member.dto.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Tag(name = "Friend API", description = "친구 관련 API")
@RequestMapping("/friends")
public interface FriendApi {

    @Operation(summary = "친구 목록 조회 API", description = "로그인한 사용자의 친구 목록(username, nickname)과 친구 수(totalFriends)를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "친구 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping()
    ApiResponse<FriendListResponseDTO> getFriendList();

    @Operation(summary = "친구 요청 보내기 API", description = "receiverId를 전달받아 친구 요청을 보냅니다. 상태는 PENDING으로 저장됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "친구 요청 성공",
                    content = @Content(schema = @Schema(implementation = FriendRequestCreateResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 요청을 보냈거나 친구 상태"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/request")
    ApiResponse<FriendRequestCreateResponseDTO> sendFriendRequest(@RequestBody FriendRequestCreateRequestDTO requestDto);

    @Operation(summary = "친구 요청 수락 API", description = "친구 요청을 수락하고 친구 관계를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "친구 요청 수락 성공",
                    content = @Content(schema = @Schema(implementation = FriendRequestAcceptResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 처리됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "요청 내역 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/accept")
    ApiResponse<FriendRequestAcceptResponseDTO> acceptFriendRequest(@RequestBody FriendRequestAcceptRequestDTO requestDto);

}