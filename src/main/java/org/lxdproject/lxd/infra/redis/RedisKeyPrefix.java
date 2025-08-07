package org.lxdproject.lxd.infra.redis;

public class RedisKeyPrefix {

    private RedisKeyPrefix() {}

    public static String recentFriendSearchKey(Long memberId) {
        return "friend:search:" + memberId;
    }
}
