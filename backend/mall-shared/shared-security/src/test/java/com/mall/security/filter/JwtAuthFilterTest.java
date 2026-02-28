package com.mall.security.filter;

import com.mall.security.config.AuthProperties;
import com.mall.security.service.JwtService;
import com.mall.security.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JwtAuthFilter 单元测试
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private JwtAuthFilter filter;
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService blacklistService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        AuthProperties props = new AuthProperties();
        props.setJwtSecret("test-secret-key-must-be-at-least-32-chars-long-for-hs256");
        props.setJwtExpirationSeconds(3600);
        props.setTokenHeader("Authorization");
        props.setTokenHead("Bearer ");
        jwtService = new JwtService(props);
        filter = new JwtAuthFilter(jwtService, blacklistService, props, "ROLE_ADMIN");
    }

    @Test
    void validToken_shouldSetAuthentication() throws Exception {
        String token = jwtService.generateToken("admin");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(blacklistService.isBlacklisted(anyString())).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void noHeader_shouldNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void blacklistedToken_shouldClearContext() throws Exception {
        String token = jwtService.generateToken("admin");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(blacklistService.isBlacklisted(anyString())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidToken_shouldClearContext() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");
        // blacklistService should not be called for invalid tokens that throw before blacklist check
        // Actually the code checks blacklist first after extracting the token string,
        // and parseUsername would throw. Let's set blacklist to return false.
        when(blacklistService.isBlacklisted(anyString())).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
