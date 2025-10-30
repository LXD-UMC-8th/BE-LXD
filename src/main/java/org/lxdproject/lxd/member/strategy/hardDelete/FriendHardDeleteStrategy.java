package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.friend.repository.FriendRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(HardDeleteOrder.FRIEND)
public class FriendHardDeleteStrategy implements HardDeleteStrategy {

    private final FriendRepository friendRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void hardDelete(LocalDateTime threshold) {
        friendRepository.hardDeleteFriendshipsOlderThanThreshold(threshold);
    }
}
