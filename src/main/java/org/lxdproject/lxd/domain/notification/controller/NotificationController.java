package org.lxdproject.lxd.domain.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.global.apiPayload.ApiResponse;
import org.lxdproject.lxd.global.common.dto.PageDTO;
import org.lxdproject.lxd.domain.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.domain.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.domain.notification.dto.ReadRedirectResponseDTO;
import org.lxdproject.lxd.domain.notification.service.NotificationService;
import org.lxdproject.lxd.domain.notification.service.SseEmitterService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class NotificationController implements NotificationApi {

    private final SseEmitterService emitterService;
    private final NotificationService notificationService;

    @Override
    public SseEmitter subscribe() {
        return emitterService.subscribe();
    }

    @Override
    public ApiResponse<String> testSend(@Valid @RequestBody NotificationRequestDTO requestDTO) {
        notificationService.createAndPublish(requestDTO);
        return ApiResponse.onSuccess("테스트 알림 발행 성공");
    }

    @Override
    public ApiResponse<PageDTO<NotificationResponseDTO>> getNotifications(int page, int size, Boolean isRead) {
        PageDTO<NotificationResponseDTO> dto = notificationService.getNotifications(isRead, page-1, size);
        return ApiResponse.onSuccess(dto);
    }

    @Override
    public ApiResponse<ReadRedirectResponseDTO> readAndRedirect(@PathVariable("notificationId") Long notificationId) {
        return ApiResponse.onSuccess(notificationService.markAsRead(notificationId));
    }

    @Override
    public ApiResponse<PageDTO<NotificationResponseDTO>> readAllNotifications(int page, int size) {
        return ApiResponse.onSuccess(notificationService.markAllAsRead(page - 1, size));
    }
}
