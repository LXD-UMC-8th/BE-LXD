package org.lxdproject.lxd.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReadRedirectResponseDTO {
    private Long notificationId;
    private String redirectUrl;
    private boolean isRead;
}

