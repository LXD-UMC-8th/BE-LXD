package org.lxdproject.lxd.notification.repository;

import org.lxdproject.lxd.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationRepositoryCustom {
    Page<Notification> findPageByMemberId(Long memberId, Boolean isRead, Pageable pageable);
    List<Notification> findUnreadWithSenderByReceiverId(Long receiverId);
    long deleteFriendRequestNotification(Long receiverId, Long requesterId);

}

