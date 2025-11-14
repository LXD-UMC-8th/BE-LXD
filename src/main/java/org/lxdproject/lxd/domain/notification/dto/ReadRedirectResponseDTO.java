package org.lxdproject.lxd.domain.notification.dto;

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

