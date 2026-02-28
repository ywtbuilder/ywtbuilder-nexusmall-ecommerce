package com.mall.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作工具 Service — 基于 StringRedisTemplate
 * <p>
 * 提供 String / Hash / List / Set / ZSet 五种数据结构常用操作，
 * 并可通过 setObject / getObject 便捷地缓存 JSON 对象。
 */
@Service
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // ==================== String ====================

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Long deleteKeys(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== Object JSON ====================

    /**
     * 以 JSON 形式缓存对象
     */
    public void setObject(String key, Object value) {
        set(key, toJson(value));
    }

    public void setObject(String key, Object value, long timeout, TimeUnit timeUnit) {
        set(key, toJson(value), timeout, timeUnit);
    }

    /**
     * 从缓存取出 JSON 对象并反序列化
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String json = get(key);
        return json == null ? null : fromJson(json, clazz);
    }

    // ==================== Hash ====================

    public void hSet(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public void hSetAll(String key, Map<String, String> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    public Long hIncrement(String key, String hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    // ==================== List ====================

    public Long lPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public Long lPushAll(String key, String... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public Long lRemove(String key, long count, String value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    // ==================== Set ====================

    public Long sAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Set<String> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // ==================== ZSet ====================

    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<String> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    public Double zScore(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    // ==================== 内部工具 ====================

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Redis JSON serialize failed", e);
        }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Redis JSON deserialize failed", e);
        }
    }
}
