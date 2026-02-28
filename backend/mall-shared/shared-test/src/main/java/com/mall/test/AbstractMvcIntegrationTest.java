package com.mall.test;

import com.mall.security.config.AuthProperties;
import com.mall.security.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc 集成测试基类 — 提供 JWT token 生成和 MockMvc 注入
 * <p>
 * 继承 AbstractIntegrationTest 获取 TestContainers 支持
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class AbstractMvcIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected AuthProperties authProperties;

    /**
     * 生成测试用的 Bearer token
     */
    protected String bearerToken(String username) {
        String token = jwtService.generateToken(username);
        return authProperties.getTokenHead() + token;
    }
}
