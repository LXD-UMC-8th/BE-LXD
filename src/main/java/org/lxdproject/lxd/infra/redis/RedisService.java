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

    @Qualifier("objectRedisTemplate")
    private final RedisTemplate<String, Object> objectRedisTemplate;

    @Qualifier("customStringRedisTemplate")
    private final RedisTemplate<String, String> stringRedisTemplate;

    // TTL(유효시간) 없이 key-value 저장
    public void setValues(String key, String data) {
        ValueOperations<String, Object> values = objectRedisTemplate.opsForValue();
        values.set(key, data);
    }

    // 유효기간(Duration) 을 설정해서 key-value 저장
    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, Object> values = objectRedisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    // key에 해당하는 String 값을 조회
    public String getValues(String key) {
        ValueOperations<String, Object> values = objectRedisTemplate.opsForValue();
        Object result = values.get(key);
        return result != null ? (String) result : null;
    }

    // 해당 key를 삭제
    public void deleteValues(String key) {
        objectRedisTemplate.delete(key);
    }

    // 이미 존재하는 key에 대해 TTL(만료시간) 을 밀리초 단위로 설정
    // ex) expireValues("abc", 1000) → 1초 후 삭제됨.
    public void expireValues(String key, Duration duration) {
        objectRedisTemplate.expire(key, duration);
    }

    // Redis의 Hash 자료구조를 사용해서 여러 필드(key-value pair)를 한 번에 저장
    public void setHashOps(String key, Map<String, String> data) {
        HashOperations<String, Object, Object> values = objectRedisTemplate.opsForHash();
        values.putAll(key, data);
    }

    // Redis의 Hash 구조에서 특정 필드(hashKey)의 값을 가져옴, 해당 hashKey 가 없으면 빈 문자열 반환
    @Transactional(readOnly = true)
    public String getHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = objectRedisTemplate.opsForHash();
        return Boolean.TRUE.equals(values.hasKey(key, hashKey)) ? (String) objectRedisTemplate.opsForHash().get(key, hashKey) : null;
    }

    // Redis Hash에서 특정 필드(hashKey)만 삭제
    public void deleteHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = objectRedisTemplate.opsForHash();
        values.delete(key, hashKey);
    }

    public boolean checkExistsValue(String value) {
        return value != null;
    }

    public List<String> getRecentSearchKeywords(String key, int limit) {
        return stringRedisTemplate.opsForList().range(key, 0, limit - 1);
    }

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

    public void removeListValue(String key, String value) {
        stringRedisTemplate.opsForList().remove(key, 0, value);
    }

    public void setString(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public boolean deleteKey(String key) {
        Boolean res = stringRedisTemplate.delete(key);
        return Boolean.TRUE.equals(res);
    }


    public List<String> getVerificationList(String token) {
        return stringRedisTemplate.opsForList().range(token, 0, -1);
    }

    // 이메일, 비밀번호 인증 토큰 저장
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

}
