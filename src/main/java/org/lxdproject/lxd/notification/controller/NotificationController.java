package org.lxdproject.lxd.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.notification.dto.NotificationCursorResponseDTO;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.service.NotificationService;
import org.lxdproject.lxd.notification.service.SseEmitterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
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
    public ApiResponse<String> testSend(@RequestBody NotificationRequestDTO requestDTO) {
        notificationService.saveAndPublishNotification(requestDTO);
        return ApiResponse.onSuccess("테스트 알림 발행 성공");
    }

    @Override
    public ApiResponse<NotificationCursorResponseDTO> getNotifications(Long lastId, int size, Boolean isRead) {
        NotificationCursorResponseDTO dto = notificationService.getNotifications(isRead, lastId, size);
        return ApiResponse.onSuccess(dto);
    }
}
