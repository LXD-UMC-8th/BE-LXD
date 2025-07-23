package org.lxdproject.lxd.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.notification.dto.NotificationMessageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification API", description = "알림 관련 API 입니다.")
@RequestMapping("/notifications")
public interface NotificationApi {

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "알림 구독 API", description = "사용자별로 알림 연결을 맺고 유지합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "알림 구독 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 필요 (JWT 누락 또는 만료)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public SseEmitter subscribe();

    @Operation(summary = "테스트 알림 전송", description = "Redis로 테스트 알림 메시지를 전송합니다.")
    @PostMapping("/test")
    public ResponseEntity<Void> testSend(@RequestBody NotificationMessageDTO messageDTO);
}
