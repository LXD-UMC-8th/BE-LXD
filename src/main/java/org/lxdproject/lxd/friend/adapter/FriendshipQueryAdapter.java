package org.lxdproject.lxd.friend.adapter;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.friend.repository.FriendRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendshipQueryAdapter implements FriendshipQueryPort {
    private final FriendRepository friendRepository;
    public boolean areFriends(Long a, Long b){ return friendRepository.areFriends(a, b); }
}
