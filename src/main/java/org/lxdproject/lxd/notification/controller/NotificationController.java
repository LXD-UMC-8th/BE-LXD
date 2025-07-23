package org.lxdproject.lxd.notification.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.notification.service.SseEmitterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final SseEmitterService emitterService;

    @Override
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribe() {
        return emitterService.connect();
    }
}
