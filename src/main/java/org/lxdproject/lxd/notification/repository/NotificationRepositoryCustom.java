package org.lxdproject.lxd.notification.repository;

import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationRepositoryCustom {
    List<NotificationResponseDTO> findNotificationsWithCursor(Long memberId, Boolean isRead, Long lastId, int size);
}

