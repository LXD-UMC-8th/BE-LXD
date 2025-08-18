package org.lxdproject.lxd.authz.guard;

public interface FriendshipQueryPort {
    boolean areFriends(Long a, Long b);
}
