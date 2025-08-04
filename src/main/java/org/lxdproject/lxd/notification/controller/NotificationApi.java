package org.lxdproject.lxd.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.PageResponse;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.dto.ReadRedirectResponseDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification API", description = "알림 관련 API 입니다.")
@RequestMapping("/notifications")
public interface NotificationApi {

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "특정 사용자 알림 구독 API", description = "사용자별로 알림 연결을 맺고 유지합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "알림 구독 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 필요 (JWT 누락 또는 만료)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    SseEmitter subscribe();

    @Operation(summary = "테스트 알림 전송 API", description = "Redis로 테스트 알림 메시지를 전송합니다.")
    @PostMapping("/test")
    ApiResponse<String> testSend(@Valid @RequestBody NotificationRequestDTO requestDTO);

    @Operation(summary = "나의 알림 조회 API", description = "나의 알림 목록을 조회합니다.")
    @GetMapping
    ApiResponse<PageResponse<NotificationResponseDTO>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isRead // 필터링 조건
    );

    @Operation(summary = "나의 알림 읽음 API", description = "알림을 읽음 처리하고 리다이렉트 url을 반환합니다.")
    @PatchMapping("/{notificationId}/read-redirect")
    ApiResponse<ReadRedirectResponseDTO> readAndRedirect(@PathVariable Long notificationId);

    @Operation(summary = "나의 알림 모두 읽음 API", description = "로그인한 사용자의 모든 안 읽은 알림을 읽음 처리합니다.")
    @PatchMapping("/read-all")
    ApiResponse<PageResponse<NotificationResponseDTO>> readAllNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
