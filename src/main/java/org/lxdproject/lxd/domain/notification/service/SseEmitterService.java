package org.lxdproject.lxd.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.global.config.security.SecurityUtil;
import org.lxdproject.lxd.domain.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.domain.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.domain.notification.entity.enums.TargetType;
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

    public SseEmitter subscribe() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간
        emitters.put(memberId, emitter);

        // 연결 종료 시 제거
        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));
        emitter.onError(e -> emitters.remove(memberId));

        try {
            log.info("[SSE] 연결 확인 이벤트 전송 - memberId: {}", memberId);
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("[SSE] 연결 확인")
            );
        } catch (IOException e) {
            log.error("[SSE] 연결 확인 이벤트 전송 실패 - memberId: {}", memberId, e);
            emitters.remove(memberId);
            emitter.completeWithError(e);
        }

        return emitter;
    }


    private void sendToClient(Long memberId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (Exception e) {
                emitters.remove(memberId);
                log.warn("[SSE] 전송 실패 → emitter 제거, memberId={}", memberId, e);
            }
        }
    }

    // 알림 생성
    public void send(Long receiverId, NotificationResponseDTO response) {
        sendToClient(receiverId, "notification-created", response);
    }

    // 알림 삭제
    public void sendNotificationDeleted(Long receiverId, Long notificationId, NotificationType type, TargetType targetType, Long targetId) {
        Map<String, Object> payload = Map.of(
                "notificationId", notificationId,
                "notificationType", type,
                "targetType", targetType,
                "targetId", targetId
        );
        sendToClient(receiverId, "notification-deleted", payload);
    }

    // 알림 단일 읽음
    public void sendNotificationReadUpdate(Long receiverId, Long notificationId) {
        Map<String, Object> payload = Map.of(
                "notificationId", notificationId,
                "isRead", true
        );
        sendToClient(receiverId, "notification-read", payload);
    }

    // 알림 전체 읽음
    public void sendAllReadUpdate(Long receiverId) {
        Map<String, Object> payload = Map.of("isAllRead", true);
        sendToClient(receiverId, "notification-all-read", payload);
    }

}

