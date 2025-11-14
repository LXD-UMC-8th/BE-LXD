package org.lxdproject.lxd.global.common.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PageDTO<T> (
        Long totalElements,
        List<T> contents,
        int page,
        int size,
        boolean hasNext
) {}
