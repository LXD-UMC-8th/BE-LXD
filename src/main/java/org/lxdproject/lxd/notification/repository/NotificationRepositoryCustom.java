package org.lxdproject.lxd.notification.repository;

import org.lxdproject.lxd.notification.entity.Notification;

import java.util.List;

public interface NotificationRepositoryCustom {
    List<Notification> findNotificationsWithCursor(Long memberId, Boolean isRead, Long lastId, int size);
    List<Notification> findUnreadWithSenderByReceiverId(Long receiverId);
}

