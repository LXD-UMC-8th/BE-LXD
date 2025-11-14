package org.lxdproject.lxd.domain.notification.message;

import lombok.*;
import org.lxdproject.lxd.domain.notification.entity.enums.EventType;
import org.lxdproject.lxd.domain.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.domain.notification.entity.enums.TargetType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageContext {
    private EventType eventType;
    private Long notificationId;
    private Long receiverId;
    private Long senderId;
    private String senderUsername;
    private NotificationType notificationType;
    private TargetType targetType;
    private Long targetId;
    private String diaryTitle;
    private String redirectUrl;
}
