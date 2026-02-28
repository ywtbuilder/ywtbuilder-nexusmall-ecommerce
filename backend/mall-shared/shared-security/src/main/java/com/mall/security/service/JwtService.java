package com.mall.security.service;

import com.mall.security.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 令牌服务 — 生成 / 解析 / 刷新
 */
@Component
public class JwtService {
    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtService(AuthProperties authProperties) {
        this.secretKey = Keys.hmacShaKeyFor(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = authProperties.getJwtExpirationSeconds();
    }

    /**
     * 根据用户名生成 JWT token
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 从 token 中解析用户名
     */
    public String parseUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * 获取 token 过期时间
     */
    public Date getExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    /**
     * 判断 token 是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpiration(token);
        return expiration.before(new Date());
    }

    /**
     * 刷新 token：重新签发一个新的
     */
    public String refreshToken(String oldToken) {
        String username = parseUsername(oldToken);
        return generateToken(username);
    }
}
