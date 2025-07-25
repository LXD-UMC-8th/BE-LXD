package org.lxdproject.lxd.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private String profileImg;
    private String nickname;
    private String username;
    private String message;
    private String redirectUrl;
    private boolean isRead;

}
