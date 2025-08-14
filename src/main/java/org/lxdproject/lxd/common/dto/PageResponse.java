package org.lxdproject.lxd.common.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResponse<T> (
        Long totalElements,
        List<T> contents,
        int page,
        int size,
        boolean hasNext
) {}
