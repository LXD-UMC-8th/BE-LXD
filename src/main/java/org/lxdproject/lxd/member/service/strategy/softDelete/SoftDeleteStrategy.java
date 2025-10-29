package org.lxdproject.lxd.member.service.strategy.softDelete;

import java.time.LocalDateTime;

public interface SoftDeleteStrategy {
    void softDelete(Long memberId, LocalDateTime deletedAt);
}
