package org.lxdproject.lxd.domain.notification.repository;

import org.lxdproject.lxd.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepositoryCustom {
    Page<Notification> findPageByMemberId(Long memberId, Boolean isRead, Pageable pageable);
    List<Notification> findUnreadWithSenderByReceiverId(Long receiverId);
    long deleteFriendRequestNotification(Long receiverId, Long requesterId);
    Long findFriendRequestNotificationId(Long receiverId, Long requesterId);
    void hardDeleteNotificationsOlderThanThreshold(LocalDateTime threshold);
}

