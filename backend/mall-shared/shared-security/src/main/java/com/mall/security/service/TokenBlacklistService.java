package com.mall.security.service;

import com.mall.common.service.RedisService;
import com.mall.security.config.AuthProperties;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT token 黑名单服务 — 基于 Redis SET
 * 用于 logout 时让 token 立即失效
 */
@Service
public class TokenBlacklistService {
    private final RedisService redisService;
    private final String keyPrefix;

    public TokenBlacklistService(RedisService redisService, AuthProperties authProperties) {
        this.redisService = redisService;
        this.keyPrefix = authProperties.getBlacklistKeyPrefix();
    }

    /**
     * 将 token 加入黑名单
     *
     * @param token     JWT token
     * @param ttlMillis 剩余有效期（毫秒），到期后自动清理
     */
    public void blacklist(String token, long ttlMillis) {
        String key = keyPrefix + token;
        redisService.set(key, "1", ttlMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 判断 token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String key = keyPrefix + token;
        return Boolean.TRUE.equals(redisService.hasKey(key));
    }
}
