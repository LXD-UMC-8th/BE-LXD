package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.friend.repository.FriendRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(5)
public class FriendHardDeleteStrategy implements HardDeleteStrategy {

    private final FriendRepository friendRepository;

    @Override
    public void hardDelete(LocalDateTime threshold) {
        friendRepository.hardDeleteFriendshipsOlderThanThreshold(threshold);
    }
}
