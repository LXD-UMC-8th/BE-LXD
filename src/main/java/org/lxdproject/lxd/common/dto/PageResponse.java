package org.lxdproject.lxd.common.dto;

import java.util.List;

public record PageResponse<T> (
        Long totalElements,
        List<T> contents,
        int page,
        int size,
        boolean hasNext
) {}
