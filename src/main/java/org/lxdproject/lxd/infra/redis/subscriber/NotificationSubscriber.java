package org.lxdproject.lxd.infra.redis.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.notification.dto.NotificationMessageDTO;
import org.lxdproject.lxd.notification.service.NotificationService;
import org.lxdproject.lxd.notification.service.SseEmitterService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {

    private final ObjectMapper objectMapper; // 메시지 역직렬화용
    private final SseEmitterService SseEmitterService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            NotificationMessageDTO dto = objectMapper.readValue(body, NotificationMessageDTO.class);

            log.debug("Redis 메시지 수신: {}", dto);

            SseEmitterService.send(dto.getReceiverId(), dto);

        } catch (Exception e) {
            log.error("Redis 메시지 처리 중 에러 발생", e);
        }
    }
}