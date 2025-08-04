package org.lxdproject.lxd.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.PageResponse;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.dto.ReadRedirectResponseDTO;
import org.lxdproject.lxd.notification.service.NotificationService;
import org.lxdproject.lxd.notification.service.SseEmitterService;
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
        return emitterService.connect();
    }

    @Override
    public ApiResponse<String> testSend(@Valid @RequestBody NotificationRequestDTO requestDTO) {
        notificationService.saveAndPublishNotification(requestDTO);
        return ApiResponse.onSuccess("테스트 알림 발행 성공");
    }

    @Override
    public ApiResponse<PageResponse<NotificationResponseDTO>> getNotifications(int page, int size, Boolean isRead) {
        PageResponse<NotificationResponseDTO> dto = notificationService.getNotifications(isRead, page-1, size);
        return ApiResponse.onSuccess(dto);
    }

    @Override
    public ApiResponse<ReadRedirectResponseDTO> readAndRedirect(@PathVariable("notificationId") Long notificationId) {
        return ApiResponse.onSuccess(notificationService.markAsReadAndSendSse(notificationId));
    }

    @Override
    public ApiResponse<String> readAllNotifications() {
        return ApiResponse.onSuccess(notificationService.markAllAsRead());
    }
}
