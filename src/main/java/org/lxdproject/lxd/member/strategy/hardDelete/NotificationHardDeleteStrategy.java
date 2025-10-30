package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(HardDeleteOrder.NOTIFICATION)
public class NotificationHardDeleteStrategy implements HardDeleteStrategy {

    private final NotificationRepository notificationRepository;

    @Override
    public void hardDelete(LocalDateTime threshold) {
        notificationRepository.hardDeleteNotificationsOlderThanThreshold(threshold);
    }
}
