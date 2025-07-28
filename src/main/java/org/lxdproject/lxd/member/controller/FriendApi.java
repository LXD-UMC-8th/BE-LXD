package org.lxdproject.lxd.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.member.dto.FriendListResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Friend API", description = "친구 관련 API")
public interface FriendApi {

    @Operation(summary = "친구 목록 조회 API", description = "로그인한 사용자의 친구 목록(username, nickname)과 친구 수(totalFriends)를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "친구 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/friends")
    ApiResponse<FriendListResponseDTO> getFriendList();
}