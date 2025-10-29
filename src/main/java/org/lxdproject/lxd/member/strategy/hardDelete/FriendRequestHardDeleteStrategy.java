package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.friend.repository.FriendRequestRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(4)
public class FriendRequestHardDeleteStrategy implements HardDeleteStrategy {

    private final FriendRequestRepository friendRequestRepository;

    @Override
    public void hardDelete(LocalDateTime threshold) {
        friendRequestRepository.hardDeleteFriendRequestsOlderThanThreshold(threshold);
    }
}
