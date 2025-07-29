package org.lxdproject.lxd.notification.repository;

import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.entity.Notification;

import java.util.List;

public interface NotificationRepositoryCustom {
    List<NotificationResponseDTO> findNotificationsWithCursor(Long memberId, Boolean isRead, Long lastId, int size);
    List<Notification> findUnreadWithSenderByReceiverId(Long receiverId);
}

