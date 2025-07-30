package org.lxdproject.lxd.notification.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.notification.dto.NotificationMessageContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    @Qualifier("jsonRedisTemplate")
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final ChannelTopic notificationTopic;

    public void publish(NotificationMessageContext message) {
        String topic = notificationTopic.getTopic();
        log.info("[Redis Publish] 알림 발행 시작 - topic: {}, receiverId: {}, notificationId: {}",
                topic, message.getReceiverId(), message.getNotificationId());

        try {
            Long subscriberCount = jsonRedisTemplate.convertAndSend(topic, message);

            log.info("[Redis Publish] 발행 성공 - topic: {}, 수신자 수: {}, message: {}",
                    topic, subscriberCount, message);
        } catch (Exception e) {
            log.error("[Redis Publish] 발행 실패 - topic: {}, message: {}", topic, message, e);
        }
    }
}
