package org.lxdproject.lxd.infra.redis.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.dto.NotificationPublishEvent;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.lxdproject.lxd.notification.service.SseEmitterService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {

    private final ObjectMapper objectMapper; // 메시지 역직렬화용
    private final SseEmitterService sseEmitterService;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis 메시지 수신
            String body = new String(message.getBody());
            NotificationPublishEvent dto = objectMapper.readValue(body, NotificationPublishEvent.class);

            log.debug("[RedisSubscriber] 메시지 수신: {}", dto);

            // 클라이언트용 메시지 생성
            Notification notification = notificationRepository.findById(dto.getNotificationId())
                    .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND));

            Member sender = memberRepository.findById(dto.getSenderId())
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

            NotificationResponseDTO response = NotificationResponseDTO.builder()
                    .profileImg(sender.getProfileImg())
                    .nickname(sender.getNickname())
                    .username(sender.getUsername())
                    .message(notification.getMessage())
                    .redirectUrl(notification.getRedirectUrl())
                    .isRead(notification.isRead())
                    .build();

            // SSE 전송
            sseEmitterService.send(dto.getReceiverId(), response);

        } catch (Exception e) {
            log.error("[RedisSubscriber] 메시지 처리 실패", e);
        }
    }
}