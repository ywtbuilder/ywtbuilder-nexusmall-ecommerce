package com.mall.security.service;

import com.mall.security.config.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtService 单元测试
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        AuthProperties props = new AuthProperties();
        props.setJwtSecret("test-secret-key-must-be-at-least-32-chars-long-for-hs256");
        props.setJwtExpirationSeconds(3600);
        jwtService = new JwtService(props);
    }

    @Test
    void generateToken_shouldReturnNonEmptyToken() {
        String token = jwtService.generateToken("testuser");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken("admin");
        String username = jwtService.parseUsername(token);
        assertEquals("admin", username);
    }

    @Test
    void getExpiration_shouldReturnFutureDate() {
        String token = jwtService.generateToken("user1");
        Date expiration = jwtService.getExpiration(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isTokenExpired_freshToken_shouldReturnFalse() {
        String token = jwtService.generateToken("user1");
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void refreshToken_shouldReturnTokenWithSameUsername() {
        String oldToken = jwtService.generateToken("user1");
        String newToken = jwtService.refreshToken(oldToken);

        assertNotNull(newToken);
        assertFalse(newToken.isEmpty());
        assertEquals("user1", jwtService.parseUsername(newToken));
    }

    @Test
    void parseUsername_invalidToken_shouldThrowException() {
        assertThrows(Exception.class, () -> jwtService.parseUsername("invalid-token"));
    }
}
