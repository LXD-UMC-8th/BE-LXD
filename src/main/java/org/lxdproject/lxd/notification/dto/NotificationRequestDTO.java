package org.lxdproject.lxd.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;

@Getter
public class NotificationRequestDTO {
    private TargetType targetType;
    private Long targetId;
    private NotificationType notificationType;
    private Long receiverId;
    @NotBlank
    @Pattern(regexp = "^/.*", message = "리다이렉트 URL은 반드시 '/'로 시작해야 합니다.")
    private String redirectUrl;
}