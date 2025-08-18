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

    // 단일 값 저장
    public void setValue(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    // 단일 값 조회
    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 단일 값 삭제
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }


    // 인증 token[type, email] 리스트 조회
    public List<String> getVerificationToken(String token) {
        return stringRedisTemplate.opsForList().range(token, 0, -1);
    }

    // // 인증 token[type, email] 저장
    public void setVerificationList(String token, String type, String email, Duration ttl) {
        stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) {
                operations.multi();
                operations.opsForList().rightPushAll(token, type, email);
                operations.expire(token, ttl);
                return operations.exec();
            }
        });
    }


    // 최근 검색어 조회
    public List<String> getRecentSearchKeywords(String key, int limit) {
        return stringRedisTemplate.opsForList().range(key, 0, limit - 1);
    }

    // 최근 검색어 갱신
    public void pushRecentSearchKeyword(String key, String keyword, int limit) {
        stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForList().remove(key, 0, keyword);
                operations.opsForList().leftPush(key, keyword);
                operations.opsForList().trim(key, 0, limit - 1);
                operations.expire(key, Duration.ofDays(30)); // 30일 TTL 설정
                return operations.exec();
            }
        });
    }

    // 검색어 해당 value 건 삭제
    public void removeListValue(String key, String value) {
        stringRedisTemplate.opsForList().remove(key, 0, value);
    }

}
