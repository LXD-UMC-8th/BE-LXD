package org.lxdproject.lxd.member.service.strategy;

import java.time.LocalDateTime;

public interface SoftDeleteStrategy {
    void softDelete(Long memberId, LocalDateTime deletedAt);
}
