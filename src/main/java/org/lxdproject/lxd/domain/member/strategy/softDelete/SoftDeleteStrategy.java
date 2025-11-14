package org.lxdproject.lxd.domain.member.strategy.softDelete;

import java.time.LocalDateTime;

public interface SoftDeleteStrategy {
    void softDelete(Long memberId, LocalDateTime deletedAt);
}
