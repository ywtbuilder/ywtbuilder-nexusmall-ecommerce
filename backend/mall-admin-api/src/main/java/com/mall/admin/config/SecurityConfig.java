package com.mall.admin.config;

import com.mall.security.component.DynamicResourcePermissionService;
import com.mall.security.component.RestAccessDeniedHandler;
import com.mall.security.component.RestAuthenticationEntryPoint;
import com.mall.security.config.AuthProperties;
import com.mall.security.filter.JwtAuthFilter;
import com.mall.security.service.JwtService;
import com.mall.security.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final AuthProperties authProperties;
    private final RestAuthenticationEntryPoint entryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final DynamicResourcePermissionService dynamicPermissionService;

    public SecurityConfig(JwtService jwtService,
                          TokenBlacklistService blacklistService,
                          AuthProperties authProperties,
                          RestAuthenticationEntryPoint entryPoint,
                          RestAccessDeniedHandler accessDeniedHandler,
                          DynamicResourcePermissionService dynamicPermissionService) {
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.authProperties = authProperties;
        this.entryPoint = entryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.dynamicPermissionService = dynamicPermissionService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                // 公开端点
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/admin/login", "/admin/register").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // 动态资源权限
                .anyRequest().access((authentication, context) ->
                    new AuthorizationDecision(
                        dynamicPermissionService.hasAccess(authentication.get(), context.getRequest())
                    ))
            )
            .addFilterBefore(
                new JwtAuthFilter(jwtService, blacklistService, authProperties, "ROLE_ADMIN"),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
