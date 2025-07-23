package org.lxdproject.lxd.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.notification.dto.NotificationMessageDTO;
import org.lxdproject.lxd.notification.publisher.NotificationPublisher;
import org.lxdproject.lxd.notification.service.NotificationService;
import org.lxdproject.lxd.notification.service.SseEmitterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final SseEmitterService emitterService;
    private final NotificationPublisher notificationPublisher;

    @Override
    public SseEmitter subscribe() {
        return emitterService.connect();
    }

    @Override
    public ResponseEntity<Void> testSend(@RequestBody NotificationMessageDTO messageDTO) {
        notificationPublisher.publish(messageDTO);
        return ResponseEntity.ok().build();
    }


}
