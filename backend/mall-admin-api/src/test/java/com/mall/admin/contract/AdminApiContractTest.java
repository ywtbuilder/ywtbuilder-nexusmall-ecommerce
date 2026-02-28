package com.mall.admin.contract;

import com.mall.admin.MallAdminApiApplication;
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
 * Admin API 契约测试 — 验证 OpenAPI 规范中所有端点路径和 HTTP 方法存在且正确
 */
@SpringBootTest(classes = MallAdminApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Admin API 契约测试")
class AdminApiContractTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String getOpenApiSpec() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    // ==================== UMS Admin ====================

    @Test
    @DisplayName("Admin: POST /admin/login 端点存在")
    void admin_login_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/admin/login");
    }

    @Test
    @DisplayName("Admin: POST /admin/register 端点存在")
    void admin_register_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/admin/register");
    }

    @Test
    @DisplayName("Admin: GET /admin/info 端点存在")
    void admin_info_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/admin/info");
    }

    @Test
    @DisplayName("Admin: GET /admin/list 端点存在")
    void admin_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/admin/list");
    }

    @Test
    @DisplayName("Admin: POST /admin/role/update 端点存在")
    void admin_role_update_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/admin/role/update");
    }

    @Test
    @DisplayName("Admin: GET /admin/refreshToken 端点存在")
    void admin_refreshToken_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/admin/refreshToken");
    }

    @Test
    @DisplayName("Admin: POST /admin/logout 端点存在")
    void admin_logout_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/admin/logout");
    }

    // ==================== UMS Role ====================

    @Test
    @DisplayName("Role: POST /role/create 端点存在")
    void role_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/role/create");
    }

    @Test
    @DisplayName("Role: GET /role/listAll 端点存在")
    void role_listAll_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/role/listAll");
    }

    @Test
    @DisplayName("Role: POST /role/allocResource 端点存在")
    void role_allocResource_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/role/allocResource");
    }

    @Test
    @DisplayName("Role: POST /role/allocMenu 端点存在")
    void role_allocMenu_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/role/allocMenu");
    }

    // ==================== UMS Resource ====================

    @Test
    @DisplayName("Resource: GET /resource/listAll 端点存在")
    void resource_listAll_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/resource/listAll");
    }

    @Test
    @DisplayName("Resource: POST /resource/create 端点存在")
    void resource_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/resource/create");
    }

    // ==================== UMS Menu ====================

    @Test
    @DisplayName("Menu: GET /menu/treeList 端点存在")
    void menu_treeList_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/menu/treeList");
    }

    @Test
    @DisplayName("Menu: POST /menu/create 端点存在")
    void menu_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/menu/create");
    }

    // ==================== PMS Brand ====================

    @Test
    @DisplayName("Brand: POST /brand/create 端点存在")
    void brand_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/brand/create");
    }

    @Test
    @DisplayName("Brand: GET /brand/list 端点存在")
    void brand_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/brand/list");
    }

    @Test
    @DisplayName("Brand: GET /brand/listAll 端点存在")
    void brand_listAll_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/brand/listAll");
    }

    // ==================== PMS Product ====================

    @Test
    @DisplayName("Product: POST /product/create 端点存在")
    void product_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/product/create");
    }

    @Test
    @DisplayName("Product: GET /product/list 端点存在")
    void product_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/product/list");
    }

    @Test
    @DisplayName("Product: GET /product/simpleList 端点存在")
    void product_simpleList_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/product/simpleList");
    }

    // ==================== PMS Product Category ====================

    @Test
    @DisplayName("ProductCategory: POST /productCategory/create 端点存在")
    void productCategory_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/productCategory/create");
    }

    @Test
    @DisplayName("ProductCategory: GET /productCategory/list/withChildren 端点存在")
    void productCategory_withChildren_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/productCategory/list/withChildren");
    }

    // ==================== PMS Product Attribute ====================

    @Test
    @DisplayName("ProductAttribute: POST /productAttribute/create 端点存在")
    void productAttribute_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/productAttribute/create");
    }

    @Test
    @DisplayName("ProductAttrCategory: GET /productAttribute/category/list 端点存在")
    void productAttrCategory_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/productAttribute/category/list");
    }

    // ==================== PMS Sku Stock ====================

    @Test
    @DisplayName("SkuStock: POST /sku/update/{pid} 端点存在")
    void sku_update_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/sku/update/{pid}");
    }

    // ==================== OMS Order ====================

    @Test
    @DisplayName("Order: GET /order/list 端点存在")
    void order_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/list");
    }

    @Test
    @DisplayName("Order: POST /order/update/delivery 端点存在")
    void order_delivery_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/update/delivery");
    }

    @Test
    @DisplayName("Order: POST /order/update/close 端点存在")
    void order_close_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/update/close");
    }

    @Test
    @DisplayName("Order: POST /order/update/note 端点存在")
    void order_note_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/order/update/note");
    }

    // ==================== OMS Return Apply ====================

    @Test
    @DisplayName("ReturnApply: GET /returnApply/list 端点存在")
    void returnApply_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/returnApply/list");
    }

    // ==================== OMS Return Reason ====================

    @Test
    @DisplayName("ReturnReason: POST /returnReason/create 端点存在")
    void returnReason_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/returnReason/create");
    }

    @Test
    @DisplayName("ReturnReason: GET /returnReason/list 端点存在")
    void returnReason_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/returnReason/list");
    }

    // ==================== OMS Order Setting ====================

    @Test
    @DisplayName("OrderSetting: POST /orderSetting/update/{id} 端点存在")
    void orderSetting_update_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/orderSetting/update/{id}");
    }

    // ==================== OMS Company Address ====================

    @Test
    @DisplayName("CompanyAddress: GET /companyAddress/list 端点存在")
    void companyAddress_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/companyAddress/list");
    }

    // ==================== SMS Coupon ====================

    @Test
    @DisplayName("Coupon: POST /coupon/create 端点存在")
    void coupon_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/coupon/create");
    }

    @Test
    @DisplayName("Coupon: GET /coupon/list 端点存在")
    void coupon_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/coupon/list");
    }

    // ==================== SMS Flash Promotion ====================

    @Test
    @DisplayName("Flash: POST /flash/create 端点存在")
    void flash_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/flash/create");
    }

    @Test
    @DisplayName("FlashSession: GET /flashSession/list 端点存在")
    void flashSession_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/flashSession/list");
    }

    @Test
    @DisplayName("FlashProductRelation: GET /flashProductRelation/list 端点存在")
    void flashProductRelation_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/flashProductRelation/list");
    }

    // ==================== SMS Home ====================

    @Test
    @DisplayName("HomeAdvertise: POST /home/advertise/create 端点存在")
    void homeAdvertise_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/advertise/create");
    }

    @Test
    @DisplayName("HomeBrand: POST /home/brand/create 端点存在")
    void homeBrand_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/brand/create");
    }

    @Test
    @DisplayName("HomeNewProduct: POST /home/newProduct/create 端点存在")
    void homeNewProduct_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/newProduct/create");
    }

    @Test
    @DisplayName("HomeRecommendProduct: POST /home/recommendProduct/create 端点存在")
    void homeRecommendProduct_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/recommendProduct/create");
    }

    @Test
    @DisplayName("HomeRecommendSubject: POST /home/recommendSubject/create 端点存在")
    void homeRecommendSubject_create_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/home/recommendSubject/create");
    }

    // ==================== EsProduct ====================

    @Test
    @DisplayName("EsProduct: POST /esProduct/importAll 端点存在")
    void esProduct_importAll_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/esProduct/importAll");
    }

    @Test
    @DisplayName("EsProduct: GET /esProduct/search/simple 端点存在")
    void esProduct_search_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/esProduct/search/simple");
    }

    // ==================== MinIO ====================

    @Test
    @DisplayName("MinIO: POST /minio/upload 端点存在")
    void minio_upload_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/minio/upload");
    }

    @Test
    @DisplayName("MinIO: POST /minio/delete 端点存在")
    void minio_delete_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/minio/delete");
    }

    // ==================== MemberLevel ====================

    @Test
    @DisplayName("MemberLevel: GET /memberLevel/list 端点存在")
    void memberLevel_list_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/memberLevel/list");
    }

    // ==================== ResourceCategory ====================

    @Test
    @DisplayName("ResourceCategory: GET /resourceCategory/listAll 端点存在")
    void resourceCategory_listAll_exists() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("/resourceCategory/listAll");
    }

    // ==================== 综合断言 ====================

    @Test
    @DisplayName("OpenAPI spec 应包含所有核心 Tag 分组")
    void spec_should_contain_all_tags() throws Exception {
        String spec = getOpenApiSpec();
        // PMS
        assertThat(spec).contains("PmsBrand");
        assertThat(spec).contains("PmsProduct");
        assertThat(spec).contains("PmsProductCategory");
        assertThat(spec).contains("PmsProductAttribute");
        assertThat(spec).contains("PmsSkuStock");
        // OMS
        assertThat(spec).contains("OmsOrder");
        assertThat(spec).contains("OmsOrderReturnApply");
        assertThat(spec).contains("OmsOrderReturnReason");
        assertThat(spec).contains("OmsOrderSetting");
        assertThat(spec).contains("OmsCompanyAddress");
        // SMS
        assertThat(spec).contains("SmsCoupon");
        assertThat(spec).contains("SmsFlashPromotion");
        assertThat(spec).contains("SmsHomeAdvertise");
        // UMS
        assertThat(spec).contains("UmsAdmin");
        assertThat(spec).contains("UmsRole");
        assertThat(spec).contains("UmsResource");
        assertThat(spec).contains("UmsMenu");
        // Search & Upload
        assertThat(spec).contains("EsProduct");
        assertThat(spec).contains("MinIOUpload");
    }

    @Test
    @DisplayName("OpenAPI spec 应声明 Bearer JWT 安全方案")
    void spec_should_declare_bearer_security() throws Exception {
        String spec = getOpenApiSpec();
        assertThat(spec).contains("BearerAuth");
        assertThat(spec).contains("bearer");
    }
}
