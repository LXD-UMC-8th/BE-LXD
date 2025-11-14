package org.lxdproject.lxd.global.infra.redis;

public class RedisKeyPrefix {

    private RedisKeyPrefix() {}

    public static String recentFriendSearchKey(Long memberId) { return "friend:search:" + memberId; } // 최근 검색 키워드
    public static String verificationTokenKey(String token) { return "verification:token:" + token; } // 인증 요청 토큰 → [type, email]
    public static String refreshTokenKey(String token) { return "auth:refresh:" + token; } // Refresh Token → Email
    public static String verificationEmailKey(String email) { return "verification:email:" + email; } // 이메일 → 마지막 인증 토큰 (중복 방지용)
}
