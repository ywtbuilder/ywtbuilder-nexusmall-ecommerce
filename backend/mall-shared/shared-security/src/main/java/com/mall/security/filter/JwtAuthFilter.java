package com.mall.security.filter;

import com.mall.security.config.AuthProperties;
import com.mall.security.service.JwtService;
import com.mall.security.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器 — 从请求头提取 token，解析用户名，设置 SecurityContext
 * <p>
 * 注意：此类不加 @Component，由各 BFF 的 SecurityConfig 手动注册，
 * 以便传入不同的默认角色 (ROLE_ADMIN / ROLE_MEMBER)。
 */
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final AuthProperties authProperties;
    private final String defaultRole;

    public JwtAuthFilter(JwtService jwtService,
                         TokenBlacklistService blacklistService,
                         AuthProperties authProperties,
                         String defaultRole) {
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.authProperties = authProperties;
        this.defaultRole = defaultRole;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(authProperties.getTokenHeader());
        String tokenHead = authProperties.getTokenHead();

        if (header != null && tokenHead != null && header.startsWith(tokenHead)) {
            String token = header.substring(tokenHead.length()).trim();
            try {
                // 检查黑名单
                if (blacklistService.isBlacklisted(token)) {
                    log.debug("Token is blacklisted");
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = jwtService.parseUsername(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                token, // 保留原始 token 在 credentials 中，便于 logout
                                List.of(new SimpleGrantedAuthority(defaultRole))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                log.warn("JWT parse failed: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
