package org.lxdproject.lxd.common.dto;

import java.util.List;

public record CursorPageResponse<T> (
        List<T> content,
        Long nextCursor,
        boolean hasNext
) {}