package org.lxdproject.lxd.domain.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.notification.repository.NotificationRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(HardDeleteOrder.NOTIFICATION)
public class NotificationHardDeleteStrategy implements HardDeleteStrategy {

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void hardDelete(LocalDateTime threshold) {
        notificationRepository.hardDeleteNotificationsOlderThanThreshold(threshold);
    }
}
