package org.lxdproject.lxd.notification.repository;

import org.lxdproject.lxd.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByReceiverId(Long receiverId, Pageable pageable);
    Page<Notification> findAllByReceiverIdAndIsRead(Long receiverId, boolean isRead, Pageable pageable);

}