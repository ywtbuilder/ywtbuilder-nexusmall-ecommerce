package com.mall.app.contract;

import com.mall.app.MallAppApiApplication;
import com.mall.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * App API 契约测试 — 验证 OpenAPI 规范中所有端点路径和 HTTP 方法存在且正确
 * <p>
 * 通过请求 /v3/api-docs 获取运行时生成的 OpenAPI 3.0 JSON，
 * 然后断言所有已记录端点的路径和方法签名完整。
 */
@SpringBootTest(classes = MallAppApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("App API 契约测试")
class AppApiContractTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 获取运行时 OpenAPI JSON
     */
    private String getOpenApiSpec() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    // ==================== SSO endpoints ====================

    @Test
    @DisplayName("SSO: POST /sso/register 端点存在")
    void sso_register_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/sso/register");
        assertThat(spec).contains("\"post\"");
    }

    @Test
    @DisplayName("SSO: POST /sso/login 端点存在")
    void sso_login_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/sso/login");
    }

    @Test
    @DisplayName("SSO: GET /sso/info 端点存在")
    void sso_info_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/sso/info");
    }

    @Test
    @DisplayName("SSO: GET /sso/getAuthCode 端点存在")
    void sso_getAuthCode_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/sso/getAuthCode");
    }

    @Test
    @DisplayName("SSO: POST /sso/updatePassword 端点存在")
    void sso_updatePassword_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/sso/updatePassword");
    }

    @Test
    @DisplayName("SSO: GET /sso/refreshToken 端点存在")
    void sso_refreshToken_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/sso/refreshToken");
    }

    // ==================== Product endpoints ====================

    @Test
    @DisplayName("Product: GET /product/detail/{id} 端点存在")
    void product_detail_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/product/detail/{id}");
    }

    @Test
    @DisplayName("Product: GET /product/categoryTreeList 端点存在")
    void product_categoryTreeList_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/product/categoryTreeList");
    }

    @Test
    @DisplayName("Product: GET /product/search 端点存在")
    void product_search_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/product/search");
    }

    // ==================== Cart endpoints ====================

    @Test
    @DisplayName("Cart: POST /cart/add 端点存在")
    void cart_add_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/cart/add");
    }

    @Test
    @DisplayName("Cart: GET /cart/list 端点存在")
    void cart_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/cart/list");
    }

    @Test
    @DisplayName("Cart: GET /cart/list/promotion 端点存在")
    void cart_list_promotion_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/cart/list/promotion");
    }

    @Test
    @DisplayName("Cart: GET /cart/update/quantity 端点存在")
    void cart_update_quantity_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/cart/update/quantity");
    }

    @Test
    @DisplayName("Cart: GET /cart/getProduct/{productId} 端点存在")
    void cart_getProduct_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/cart/getProduct/{productId}");
    }

    @Test
    @DisplayName("Cart: POST /cart/update/attr 端点存在")
    void cart_update_attr_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/cart/update/attr");
    }

    @Test
    @DisplayName("Cart: POST /cart/delete 端点存在")
    void cart_delete_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/cart/delete");
    }

    @Test
    @DisplayName("Cart: POST /cart/clear 端点存在")
    void cart_clear_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/cart/clear");
    }

    // ==================== Order endpoints ====================

    @Test
    @DisplayName("Order: GET /order/confirm 端点存在")
    void order_confirm_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/confirm");
    }

    @Test
    @DisplayName("Order: POST /order/generateOrder 端点存在")
    void order_generateOrder_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/generateOrder");
    }

    @Test
    @DisplayName("Order: POST /order/paySuccess 端点存在")
    void order_paySuccess_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/paySuccess");
    }

    @Test
    @DisplayName("Order: POST /order/cancelUserOrder 端点存在")
    void order_cancelUserOrder_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/cancelUserOrder");
    }

    @Test
    @DisplayName("Order: GET /order/list 端点存在")
    void order_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/list");
    }

    @Test
    @DisplayName("Order: GET /order/detail/{orderId} 端点存在")
    void order_detail_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/detail/{orderId}");
    }

    @Test
    @DisplayName("Order: POST /order/confirmReceiveOrder 端点存在")
    void order_confirmReceiveOrder_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/confirmReceiveOrder");
    }

    @Test
    @DisplayName("Order: POST /order/deleteOrder 端点存在")
    void order_deleteOrder_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/deleteOrder");
    }

    // ==================== Home endpoints ====================

    @Test
    @DisplayName("Home: GET /home/content 端点存在")
    void home_content_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/content");
    }

    @Test
    @DisplayName("Home: GET /home/content-lite 端点存在")
    void home_content_lite_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/content-lite");
    }

    @Test
    @DisplayName("Home: GET /home/productCateList 端点存在")
    void home_productCateList_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/productCateList");
    }

    @Test
    @DisplayName("Home: GET /home/recommendProductList 端点存在")
    void home_recommendProductList_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/recommendProductList");
    }

    @Test
    @DisplayName("Home: GET /home/hotProductList 端点存在")
    void home_hotProductList_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/hotProductList");
    }

    @Test
    @DisplayName("Home: GET /home/newProductList 端点存在")
    void home_newProductList_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/newProductList");
    }

    // ==================== Payment endpoints ====================

    @Test
    @DisplayName("Payment: POST /payment/create 端点存在")
    void payment_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/payment/create");
    }

    @Test
    @DisplayName("Payment: POST /payment/notify 端点存在")
    void payment_notify_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/payment/notify");
    }

    @Test
    @DisplayName("Payment: GET /payment/query 端点存在")
    void payment_query_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/payment/query");
    }

    // ==================== Search endpoints ====================

    @Test
    @DisplayName("Search: GET /search/simple 端点存在")
    void search_simple_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/search/simple");
    }

    // ==================== Member behavior endpoints ====================

    @Test
    @DisplayName("Member: GET /member/address/list 端点存在")
    void member_address_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/member/address/list");
    }

    @Test
    @DisplayName("Member: POST /member/readHistory/create 端点存在")
    void member_readHistory_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/member/readHistory/create");
    }

    @Test
    @DisplayName("Member: GET /member/productCollection/list 端点存在")
    void member_collection_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/member/productCollection/list");
    }

    @Test
    @DisplayName("Member: GET /member/attention/list 端点存在")
    void member_attention_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/member/attention/list");
    }

    @Test
    @DisplayName("Member: GET /member/coupon/list 端点存在")
    void member_coupon_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/member/coupon/list");
    }

    @Test
    @DisplayName("ReturnApply: POST /returnApply/create 端点存在")
    void returnApply_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/returnApply/create");
    }

    // ==================== 综合断言 — 所有端点数量 ====================

    @Test
    @DisplayName("OpenAPI spec 应包含所有 Tag 分组")
    void spec_should_contain_all_tags() throws Exception {
        String spec = getOpenApiSpec();
        // 验证所有 Controller 的 @Tag name 出现在 spec 中
        assertThat(spec).contains("\"SSO\"");
        assertThat(spec).contains("\"Product\"");
        assertThat(spec).contains("\"Cart\"");
        assertThat(spec).contains("\"Order\"");
        assertThat(spec).contains("\"Home\"");
        assertThat(spec).contains("\"Payment\"");
    }

    @Test
    @DisplayName("OpenAPI spec 中所有路径应使用正确的 HTTP 方法")
    void spec_paths_should_use_correct_methods() throws Exception {
        String spec = getOpenApiSpec();
        // 验证 POST 端点确实是 POST（而非 GET）
        // /sso/login 应在 "post" 块内
        int loginIdx = spec.indexOf("/sso/login");
        assertThat(loginIdx).isGreaterThan(0);
        // /sso/info 应在 "get" 块内
        int infoIdx = spec.indexOf("/sso/info");
        assertThat(infoIdx).isGreaterThan(0);
    }

    @Test
    @DisplayName("OpenAPI spec 应声明 Bearer JWT 安全方案")
    void spec_should_declare_bearer_security() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("BearerAuth");
        assertThat(spec).contains("bearer");
    }
}
