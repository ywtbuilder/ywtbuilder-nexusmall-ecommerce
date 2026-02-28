package com.mall.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 认证配置属性
 */
@Data
@ConfigurationProperties(prefix = "mall.auth")
public class AuthProperties {
    /** HTTP 请求头名称，默认 Authorization */
    private String tokenHeader = "Authorization";
    /** token 前缀，默认 "Bearer " */
    private String tokenHead = "Bearer ";
    /** JWT 签名密钥（至少 32 字符） */
    private String jwtSecret;
    /** JWT 过期时间（秒），默认 7 天 */
    private long jwtExpirationSeconds = 604800;
    /** token 黑名单 Redis key 前缀 */
    private String blacklistKeyPrefix = "jwt:blacklist:";
}
