package org.lxdproject.lxd.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadUpdateDTO {
    @NotNull(message = "알림 ID는 필수입니다.")
    private Long notificationId;
    private boolean isRead;
}

