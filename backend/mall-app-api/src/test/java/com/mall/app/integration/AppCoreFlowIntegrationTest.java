package com.mall.app.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.app.MallAppApiApplication;
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
 * App 核心购物流程集成测试
 * <p>
 * 端到端测试链路：注册 → 登录 → 浏览商品 → 加入购物车 → 查看购物车 → 清空购物车
 * <p>
 * 使用 TestContainers MySQL 8.0 + Redis 7 提供真实中间件环境。
 */
@SpringBootTest(classes = MallAppApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "classpath:sql/schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("App 核心购物流程集成测试")
class AppCoreFlowIntegrationTest extends AbstractMvcIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    /** 测试用户名 */
    private static final String TEST_USERNAME = "integration_test_user";
    private static final String TEST_PASSWORD = "Test@12345";
    private static final String TEST_PHONE = "13800138000";

    /** 保存登录后获取的 token */
    private static String loginToken;

    // ==================== 1. 注册 ====================

    @Test
    @Order(1)
    @DisplayName("1. 注册新用户 — 无验证码场景")
    void step1_register() throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "username", TEST_USERNAME,
                        "password", TEST_PASSWORD,
                        "telephone", TEST_PHONE
                ));

        mockMvc.perform(post("/sso/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    @DisplayName("2. 重复注册应失败")
    void step2_register_duplicate() throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "username", TEST_USERNAME,
                        "password", TEST_PASSWORD,
                        "telephone", TEST_PHONE
                ));

        MvcResult result = mockMvc.perform(post("/sso/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        // 期望返回失败（用户名已被注册）
        assertThat(json.get("code").asInt()).isNotEqualTo(200);
    }

    // ==================== 2. 登录 ====================

    @Test
    @Order(3)
    @DisplayName("3. 使用正确密码登录")
    void step3_login_success() throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "username", TEST_USERNAME,
                        "password", TEST_PASSWORD
                ));

        MvcResult result = mockMvc.perform(post("/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        loginToken = json.get("data").get("tokenHead").asText() + json.get("data").get("token").asText();
        assertThat(loginToken).isNotBlank();
    }

    @Test
    @Order(4)
    @DisplayName("4. 使用错误密码登录应失败")
    void step4_login_wrong_password() throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "username", TEST_USERNAME,
                        "password", "WrongPassword"
                ));

        MvcResult result = mockMvc.perform(post("/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("code").asInt()).isNotEqualTo(200);
    }

    // ==================== 3. 获取用户信息 ====================

    @Test
    @Order(5)
    @DisplayName("5. 获取当前登录用户信息")
    void step5_get_member_info() throws Exception {
        mockMvc.perform(get("/sso/info")
                        .header("Authorization", loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
    }

    @Test
    @Order(6)
    @DisplayName("6. 未认证访问受保护端点应返回 401")
    void step6_unauthenticated_access_denied() throws Exception {
        mockMvc.perform(get("/sso/info"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 4. 浏览商品（公开端点） ====================

    @Test
    @Order(10)
    @DisplayName("10. 浏览商品分类树 — 无需认证")
    void step10_product_category_tree() throws Exception {
        mockMvc.perform(get("/product/categoryTreeList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(11)
    @DisplayName("11. 搜索商品 — 无需认证")
    void step11_product_search() throws Exception {
        mockMvc.perform(get("/product/search")
                        .param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(12)
    @DisplayName("12. 获取商品详情 — 无需认证")
    void step12_product_detail() throws Exception {
        mockMvc.perform(get("/product/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 5. 首页内容（公开端点） ====================

    @Test
    @Order(15)
    @DisplayName("15. 获取首页内容 — 无需认证")
    void step15_home_content() throws Exception {
        mockMvc.perform(get("/home/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(16)
    @DisplayName("16. 获取首页轻量内容 — 无需认证")
    void step16_home_content_lite() throws Exception {
        mockMvc.perform(get("/home/content-lite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(17)
    @DisplayName("17. 获取推荐商品列表 — 无需认证")
    void step17_home_recommend() throws Exception {
        mockMvc.perform(get("/home/recommendProductList")
                        .param("pageNum", "1")
                        .param("pageSize", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 6. 购物车操作（需认证） ====================

    @Test
    @Order(20)
    @DisplayName("20. 添加商品到购物车")
    void step20_cart_add() throws Exception {
        String cartItem = objectMapper.writeValueAsString(java.util.Map.of(
                "productId", 1,
                "productSkuId", 1,
                "quantity", 2
        ));

        mockMvc.perform(post("/cart/add")
                        .header("Authorization", loginToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartItem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(21)
    @DisplayName("21. 查看购物车列表 — 应包含刚添加的商品")
    void step21_cart_list() throws Exception {
        MvcResult result = mockMvc.perform(get("/cart/list")
                        .header("Authorization", loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = json.get("data");
        assertThat(data.size()).isGreaterThanOrEqualTo(1);
        // productName 由后端从 pms_product 查询，seed 中 id=1 为 iPhone 15 Pro Max
        assertThat(data.get(0).get("productName").asText()).isEqualTo("iPhone 15 Pro Max");
        assertThat(data.get(0).get("price").decimalValue()).isGreaterThan(java.math.BigDecimal.ZERO);
        assertThat(data.get(0).get("quantity").asInt()).isEqualTo(2);
    }

    @Test
    @Order(22)
    @DisplayName("22. 修改购物车商品数量")
    void step22_cart_update_quantity() throws Exception {
        // 先获取购物车列表取得 id
        MvcResult listResult = mockMvc.perform(get("/cart/list")
                        .header("Authorization", loginToken))
                .andReturn();
        JsonNode items = objectMapper.readTree(listResult.getResponse().getContentAsString()).get("data");
        long cartItemId = items.get(0).get("id").asLong();

        mockMvc.perform(get("/cart/update/quantity")
                        .header("Authorization", loginToken)
                        .param("id", String.valueOf(cartItemId))
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证数量已更新
        MvcResult updatedResult = mockMvc.perform(get("/cart/list")
                        .header("Authorization", loginToken))
                .andReturn();
        JsonNode updatedItems = objectMapper.readTree(updatedResult.getResponse().getContentAsString()).get("data");
        assertThat(updatedItems.get(0).get("quantity").asInt()).isEqualTo(5);
    }

    @Test
    @Order(23)
    @DisplayName("23. 再次添加相同商品 — 应累加数量")
    void step23_cart_add_same_product() throws Exception {
        String cartItem = objectMapper.writeValueAsString(java.util.Map.of(
                "productId", 1,
                "productSkuId", 1,
                "quantity", 3
        ));

        mockMvc.perform(post("/cart/add")
                        .header("Authorization", loginToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartItem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证累加：5 + 3 = 8
        MvcResult result = mockMvc.perform(get("/cart/list")
                        .header("Authorization", loginToken))
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        assertThat(data.size()).isEqualTo(1); // 仍然是 1 个购物车项
        assertThat(data.get(0).get("quantity").asInt()).isEqualTo(8);
    }

    @Test
    @Order(30)
    @DisplayName("30. 清空购物车")
    void step30_cart_clear() throws Exception {
        mockMvc.perform(post("/cart/clear")
                        .header("Authorization", loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证清空后列表为空
        MvcResult result = mockMvc.perform(get("/cart/list")
                        .header("Authorization", loginToken))
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        assertThat(data.size()).isEqualTo(0);
    }

    // ==================== 7. Token 刷新 ====================

    @Test
    @Order(40)
    @DisplayName("40. 刷新 Token")
    void step40_refresh_token() throws Exception {
        MvcResult result = mockMvc.perform(get("/sso/refreshToken")
                        .header("Authorization", loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String newToken = json.get("data").get("tokenHead").asText() + json.get("data").get("token").asText();
        assertThat(newToken).isNotBlank();
        // 更新 token 用于后续测试
        loginToken = newToken;
    }
}
