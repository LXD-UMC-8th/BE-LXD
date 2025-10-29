package org.lxdproject.lxd.member.strategy.hardDelete;

import java.time.LocalDateTime;

public interface HardDeleteStrategy {
    void hardDelete(LocalDateTime threshold);
}
