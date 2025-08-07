package org.lxdproject.lxd.infra.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

    @Qualifier("redisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    // TTL(유효시간) 없이 key-value 저장
    public void setValues(String key, String data) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    // 유효기간(Duration) 을 설정해서 key-value 저장
    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    // key에 해당하는 String 값을 조회
    public String getValues(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        Object result = values.get(key);
        return result != null ? (String) result : null;
    }

    // 해당 key를 삭제
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    // 이미 존재하는 key에 대해 TTL(만료시간) 을 밀리초 단위로 설정
    // ex) expireValues("abc", 1000) → 1초 후 삭제됨.
    public void expireValues(String key, Duration duration) {
        redisTemplate.expire(key, duration);
    }

    // Redis의 Hash 자료구조를 사용해서 여러 필드(key-value pair)를 한 번에 저장
    public void setHashOps(String key, Map<String, String> data) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.putAll(key, data);
    }

    // Redis의 Hash 구조에서 특정 필드(hashKey)의 값을 가져옴, 해당 hashKey 가 없으면 빈 문자열 반환
    @Transactional(readOnly = true)
    public String getHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        return Boolean.TRUE.equals(values.hasKey(key, hashKey)) ? (String) redisTemplate.opsForHash().get(key, hashKey) : null;
    }

    // Redis Hash에서 특정 필드(hashKey)만 삭제
    public void deleteHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.delete(key, hashKey);
    }

    public boolean checkExistsValue(String value) {
        return value != null;
    }

    // List 왼쪽에 값 추가 (LPUSH)
    public void pushToList(String key, String value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    // List 값 범위 조회 (LRANGE)
    public List<Object> getListRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // List 중복 제거 (LREM)
    public void removeListValue(String key, String value) {
        redisTemplate.opsForList().remove(key, 0, value); // 0: 모두 제거
    }

    // List 길이 자르기 (LTRIM)
    public void trimList(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }
}
