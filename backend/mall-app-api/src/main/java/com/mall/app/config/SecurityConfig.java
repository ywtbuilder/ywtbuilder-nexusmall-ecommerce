package com.mall.app.config;

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

    public SecurityConfig(JwtService jwtService,
                          TokenBlacklistService blacklistService,
                          AuthProperties authProperties,
                          RestAuthenticationEntryPoint entryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.authProperties = authProperties;
        this.entryPoint = entryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
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
                .requestMatchers(HttpMethod.POST, "/sso/login", "/sso/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/rum").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/sso/getAuthCode",
                    "/home/content",
                    "/home/content-lite",
                    "/home/productCateList/**",
                    "/home/recommendProductList",
                    "/home/hotProductList",
                    "/home/newProductList",
                    "/home/subjectList",
                    "/product/detail/**",
                    "/product/categoryTreeList",
                    "/product/search",
                    "/productCategory/list/withChildren",
                    "/brand/recommendList",
                    "/brand/detail/**",
                    "/brand/productList",
                    "/search/**",
                    "/asset/image/**",
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // 支付回调（无需登录）
                .requestMatchers(HttpMethod.POST, "/payment/notify").permitAll()
                // 其余需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new RequestTraceFilter(),
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(
                new JwtAuthFilter(jwtService, blacklistService, authProperties, "ROLE_MEMBER"),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
