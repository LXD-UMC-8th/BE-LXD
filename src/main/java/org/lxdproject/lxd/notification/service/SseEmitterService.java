package org.lxdproject.lxd.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.notification.dto.NotificationReadUpdateDTO;
import org.lxdproject.lxd.notification.entity.Notification;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간
        emitters.put(memberId, emitter);

        // 연결 종료 시 제거
        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));
        emitter.onError(e -> emitters.remove(memberId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected")
            );
        } catch (IOException e) {
            emitters.remove(memberId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void send(Long memberId, Object data) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            try {
                log.info("[SSE] 알림 전송 - to: {}, data: {}", memberId, data);
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(memberId);
                log.error("[SSE] 알림 전송 실패 - memberId: {}", memberId, e);
            }
        } else {
            log.warn("[SSE] 알림 전송 실패: 연결 없음 - memberId: {}", memberId);
        }
    }

    public void sendNotificationReadUpdate(Notification notification) {
        Long receiverId = notification.getReceiver().getId();

        NotificationReadUpdateDTO dto = new NotificationReadUpdateDTO(
                notification.getId(), true
        );

        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                log.info("[SSE] 알림 읽음 상태 전송 - to: {}, notificationId: {}", receiverId, notification.getId());
                emitter.send(SseEmitter.event()
                        .name("notification-read")
                        .data(dto));
            } catch (IOException e) {
                emitters.remove(receiverId);
                log.error("[SSE] 알림 읽음 상태 전송 실패 - receiverId: {}", receiverId, e);
            }
        }
        else {
            log.warn("[SSE] 알림 읽음 상태 전송 실패: 연결 없음 - receiverId: {}", receiverId);
        }
    }

    public void sendAllReadUpdate(Long receiverId) {
        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification-all-read")
                        .data("ALL_READ"));
            } catch (IOException e) {
                emitters.remove(receiverId);
            }
        }
    }


}

