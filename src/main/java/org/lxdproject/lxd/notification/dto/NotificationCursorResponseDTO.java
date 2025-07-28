package org.lxdproject.lxd.notification.dto;

import java.util.List;

public record NotificationCursorResponseDTO (
    List<NotificationResponseDTO> content,
    Long nextCursor,
    boolean hasNext
) {}
