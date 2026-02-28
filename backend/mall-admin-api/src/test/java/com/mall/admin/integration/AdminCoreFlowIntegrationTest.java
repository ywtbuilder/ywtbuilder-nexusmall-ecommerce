package com.mall.admin.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.admin.MallAdminApiApplication;
import com.mall.test.AbstractMvcIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Admin 核心管理流程集成测试
 * <p>
 * 测试链路：login(stub) → 商品列表(stub) → 订单列表(需 RBAC 放行) → 品牌列表
 * <p>
 * 注意：V3 的 Admin Controllers 大部分是 stub（返回固定值），
 * 本测试主要验证 RBAC 安全链路是否正确工作。
 */
@SpringBootTest(classes = MallAdminApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "classpath:sql/schema-admin-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("Admin 核心管理流程集成测试")
class AdminCoreFlowIntegrationTest extends AbstractMvcIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 1. 登录端点 ====================

    @Test
    @Order(1)
    @DisplayName("1. Admin 登录端点可访问（公开端点）")
    void step1_login_endpoint_accessible() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "username", "admin",
                "password", "admin123"
        ));

        // Stub Controller 直接返回固定 token
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    @DisplayName("2. Admin 注册端点可访问（公开端点）")
    void step2_register_endpoint_accessible() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "username", "newadmin",
                "password", "pass123"
        ));

        mockMvc.perform(post("/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 2. 未认证访问受保护端点 ====================

    @Test
    @Order(5)
    @DisplayName("5. 未认证访问 /admin/info 应返回 401")
    void step5_unauthenticated_admin_info() throws Exception {
        mockMvc.perform(get("/admin/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("6. 未认证访问 /product/list 应返回 401")
    void step6_unauthenticated_product_list() throws Exception {
        mockMvc.perform(get("/product/list"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 3. 使用 JWT 访问受保护端点 ====================

    @Test
    @Order(10)
    @DisplayName("10. 使用超级管理员 JWT 访问 /admin/info — 应成功")
    void step10_admin_info_with_jwt() throws Exception {
        // 使用 bearerToken 生成 "admin" 用户的 JWT
        // DynamicResourcePermissionService 对 username=admin 直接放行
        String token = bearerToken("admin");

        mockMvc.perform(get("/admin/info")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(11)
    @DisplayName("11. 使用 JWT 访问 /product/list — 超级管理员放行")
    void step11_product_list_with_jwt() throws Exception {
        String token = bearerToken("admin");

        mockMvc.perform(get("/product/list")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(12)
    @DisplayName("12. 使用 JWT 访问 /order/list — 超级管理员放行")
    void step12_order_list_with_jwt() throws Exception {
        String token = bearerToken("admin");

        mockMvc.perform(get("/order/list")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(13)
    @DisplayName("13. 使用 JWT 访问 /brand/list — 超级管理员放行")
    void step13_brand_list_with_jwt() throws Exception {
        String token = bearerToken("admin");

        mockMvc.perform(get("/brand/list")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(14)
    @DisplayName("14. 使用 JWT 访问 /coupon/list — 超级管理员放行")
    void step14_coupon_list_with_jwt() throws Exception {
        String token = bearerToken("admin");

        mockMvc.perform(get("/coupon/list")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(15)
    @DisplayName("15. 使用 JWT 访问 /returnReason/list — 超级管理员放行")
    void step15_return_reason_list_with_jwt() throws Exception {
        String token = bearerToken("admin");

        mockMvc.perform(get("/returnReason/list")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 4. Swagger / Actuator 公开端点 ====================

    @Test
    @Order(20)
    @DisplayName("20. /v3/api-docs 无需认证可访问")
    void step20_api_docs_public() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(21)
    @DisplayName("21. /actuator/health 无需认证可访问")
    void step21_actuator_health_public() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ==================== 5. RBAC 权限隔离 ====================

    @Test
    @Order(30)
    @DisplayName("30. 非超级管理员用户 — 无 ums_resource 匹配时放行")
    void step30_non_admin_no_resource_match() throws Exception {
        // ums_resource 表为空时，DynamicResourcePermissionService 加载资源列表为空
        // AntPathMatcher 无匹配 → 放行
        String token = bearerToken("normaluser");

        mockMvc.perform(get("/brand/list")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
