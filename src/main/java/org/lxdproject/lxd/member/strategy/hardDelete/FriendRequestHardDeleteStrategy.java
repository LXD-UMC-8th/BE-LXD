package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.friend.repository.FriendRequestRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(HardDeleteOrder.FRIEND_REQUEST)
public class FriendRequestHardDeleteStrategy implements HardDeleteStrategy {

    private final FriendRequestRepository friendRequestRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void hardDelete(LocalDateTime threshold) {
        friendRequestRepository.hardDeleteFriendRequestsOlderThanThreshold(threshold);
    }
}
