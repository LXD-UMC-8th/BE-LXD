package org.lxdproject.lxd.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private Long id; // 커서 기준
    private boolean buttonField;
    private String profileImg;
    private List<MessagePart> messageParts;
    private String redirectUrl;
    private boolean isRead;
    private String createdAt;
}
