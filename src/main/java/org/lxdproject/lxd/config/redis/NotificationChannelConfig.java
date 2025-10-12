package org.lxdproject.lxd.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

@Configuration
public class NotificationChannelConfig {

    @Bean
    public ChannelTopic notificationTopic() {
        return new ChannelTopic("notification:event");
    }
}
