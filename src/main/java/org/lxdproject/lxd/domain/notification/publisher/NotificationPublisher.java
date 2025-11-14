package org.lxdproject.lxd.domain.notification.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.domain.notification.message.MessageContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    @Qualifier("jsonRedisTemplate")
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final ChannelTopic notificationTopic;

    public void publishAfterCommit(MessageContext message) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(message);
                }
            });
        } else {
            publish(message);
        }
    }

    public void publish(MessageContext message) {
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

