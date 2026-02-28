package com.mall.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务 — 基于 Redis 存储验证码，支持手机 / 邮箱注册、密码重置等场景
 */
@Service
public class AuthCodeService {
    private static final Logger log = LoggerFactory.getLogger(AuthCodeService.class);
    private static final String AUTH_CODE_PREFIX = "authCode:";
    /** 验证码有效期（秒） */
    private static final long AUTH_CODE_EXPIRE = 300;
    /** 验证码位数 */
    private static final int CODE_LENGTH = 6;

    private final RedisService redisService;
    private final Random random = new Random();

    public AuthCodeService(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * 生成验证码并存入 Redis
     *
     * @param identity 手机号或邮箱
     * @return 验证码字符串
     */
    public String generateAuthCode(String identity) {
        String code = generateCode();
        String key = AUTH_CODE_PREFIX + identity;
        redisService.set(key, code, AUTH_CODE_EXPIRE, TimeUnit.SECONDS);
        log.info("Generated auth code for [{}]: {}", identity, code);
        return code;
    }

    /**
     * 校验验证码
     *
     * @param identity 手机号或邮箱
     * @param code     用户输入的验证码
     * @return 是否匹配
     */
    public boolean verifyAuthCode(String identity, String code) {
        if (identity == null || code == null) {
            return false;
        }
        String key = AUTH_CODE_PREFIX + identity;
        String storedCode = redisService.get(key);
        if (code.equals(storedCode)) {
            // 验证成功后立即删除，防止重复使用
            redisService.delete(key);
            return true;
        }
        return false;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
