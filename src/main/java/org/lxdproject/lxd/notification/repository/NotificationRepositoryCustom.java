package org.lxdproject.lxd.notification.repository;

import org.lxdproject.lxd.notification.entity.Notification;

import java.util.List;

public interface NotificationRepositoryCustom {
    List<Notification> findNotificationsWithMemberId(Long memberId, Boolean isRead, int page, int size);
    List<Notification> findUnreadWithSenderByReceiverId(Long receiverId);
}

