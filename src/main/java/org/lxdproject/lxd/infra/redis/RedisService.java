package org.lxdproject.lxd.infra.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.auth.enums.VerificationType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisService {

    @Qualifier("StrRedisTemplate")
    private final RedisTemplate<String, String> stringRedisTemplate;

    /**
     * @: Refresh Token 관리
     * Key: auth:refresh:{token}
     * Value: email
     */
    public void setRefreshToken(String token, String email, Duration ttl) {
        String key = RedisKeyPrefix.refreshTokenKey(token);
        stringRedisTemplate.opsForValue().set(key, email, ttl);
    }

    public String getRefreshTokenEmail(String token) {
        String key = RedisKeyPrefix.refreshTokenKey(token);
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String token) {
        String key = RedisKeyPrefix.refreshTokenKey(token);
        stringRedisTemplate.delete(key);
    }


    /**
     * @: 인증 요청 토큰 관리
     * Key: verification:token:{token}
     * Value: [type, email]
     */
    public void setVerificationToken(String token, String type, String email, Duration ttl) {
        String key = RedisKeyPrefix.verificationTokenKey(token);
        stringRedisTemplate.execute(new SessionCallback<>() {
            @Override
            public List<Object> execute(RedisOperations operations) {
                operations.multi();
                operations.opsForList().rightPushAll(key, type, email);
                operations.expire(key, ttl);
                return operations.exec();
            }
        });
    }

    public List<String> getVerificationToken(String token) {
        String key = RedisKeyPrefix.verificationTokenKey(token);
        return stringRedisTemplate.opsForList().range(key, 0, -1);
    }


    /**
     * @: 이메일 → 마지막 인증 토큰 (중복 방지)
     * Key: verification:email:{email}
     * Value: token
     */
    public void setVerificationEmail(String email, String token, Duration ttl) {
        String key = RedisKeyPrefix.verificationEmailKey(email);
        stringRedisTemplate.opsForValue().set(key, token, ttl);
    }

    public String getVerificationEmailToken(String email) {
        String key = RedisKeyPrefix.verificationEmailKey(email);
        return stringRedisTemplate.opsForValue().get(key);
    }


    /**
     * @: 최근 친구 검색 기록
     * Key: friend:search:{memberId}
     * Value: [keywords...]
     */
    public void pushRecentSearchKeyword(Long memberId, String keyword, int limit) {
        String key = RedisKeyPrefix.recentFriendSearchKey(memberId);

        stringRedisTemplate.execute(new SessionCallback<>() {
            @Override
            public List<Object> execute(RedisOperations operations) {
                operations.multi();
                operations.opsForList().remove(key, 0, keyword); // 중복 제거
                operations.opsForList().leftPush(key, keyword);  // 최신값 추가
                operations.opsForList().trim(key, 0, limit - 1); // 최대 limit 개수 유지
                operations.expire(key, Duration.ofDays(30));     // TTL 30일
                return operations.exec();
            }
        });
    }

    public List<String> getRecentSearchKeywords(Long memberId, int limit) {
        String key = RedisKeyPrefix.recentFriendSearchKey(memberId);
        return stringRedisTemplate.opsForList().range(key, 0, limit - 1);
    }

    public void removeRecentSearchKeyword(Long memberId, String keyword) {
        String key = RedisKeyPrefix.recentFriendSearchKey(memberId);
        stringRedisTemplate.opsForList().remove(key, 0, keyword);
    }

    public void deleteRecentSearchKeywords(Long memberId) {
        String key = RedisKeyPrefix.recentFriendSearchKey(memberId);
        stringRedisTemplate.delete(key);
    }

}
