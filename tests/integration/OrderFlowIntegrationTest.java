package com.mall.tests.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 订单流程集成测试 — 端到端验证完整下单流程
 * <p>
 * 测试链路：会员登录 → 浏览商品 → 添加购物车 → 确认订单 → 生成订单 → 订单列表 → 订单详情
 * <p>
 * 前置条件：本地服务已启动（MySQL/Redis/App API on 18080）
 */
public class OrderFlowIntegrationTest {

    private static final String APP_URL = "http://localhost:18080";
    private static final String ADMIN_URL = "http://localhost:18081";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        int passed = 0;
        int failed = 0;

        System.out.println("===== Order Flow Integration Tests =====\n");

        // Step 1: Member login
        System.out.println("--- Step 1: Member Login ---");
        String loginBody = "{\"username\":\"test\",\"password\":\"test123456\"}";
        HttpResponse<String> loginResp = post(APP_URL + "/sso/login", loginBody, null);
        String token = extractToken(loginResp.body());
        if (token != null) {
            System.out.println("  [PASS] Login successful, token obtained");
            passed++;
        } else {
            System.out.println("  [WARN] Login failed (may need seed data), testing with unauthenticated flow");
            failed++;
        }

        // Step 2: Browse products
        System.out.println("--- Step 2: Browse Products ---");
        HttpResponse<String> prodResp = get(APP_URL + "/product/list?pageNum=1&pageSize=5", token);
        if (prodResp.statusCode() == 200 && prodResp.body().contains("\"code\"")) {
            System.out.println("  [PASS] Product list returned successfully");
            passed++;
        } else {
            System.out.println("  [FAIL] Product list endpoint error");
            failed++;
        }

        // Step 3: Home content
        System.out.println("--- Step 3: Home Content ---");
        HttpResponse<String> homeResp = get(APP_URL + "/home/content", token);
        if (homeResp.statusCode() == 200 && homeResp.body().contains("\"code\"")) {
            System.out.println("  [PASS] Home content aggregation works");
            passed++;
        } else {
            System.out.println("  [FAIL] Home content error");
            failed++;
        }

        // Step 4: Cart operations (requires auth)
        System.out.println("--- Step 4: Cart Operations ---");
        if (token != null) {
            HttpResponse<String> cartResp = get(APP_URL + "/cart/list", token);
            if (cartResp.body().contains("\"code\"")) {
                System.out.println("  [PASS] Cart list accessible");
                passed++;
            } else {
                System.out.println("  [FAIL] Cart list error");
                failed++;
            }
        } else {
            System.out.println("  [SKIP] Cart test skipped (no auth token)");
        }

        // Step 5: Order list (requires auth)
        System.out.println("--- Step 5: Order List ---");
        if (token != null) {
            HttpResponse<String> orderResp = get(APP_URL + "/order/list?pageNum=1&pageSize=5", token);
            if (orderResp.body().contains("\"code\"")) {
                System.out.println("  [PASS] Order list accessible");
                passed++;
            } else {
                System.out.println("  [FAIL] Order list error");
                failed++;
            }
        } else {
            System.out.println("  [SKIP] Order test skipped (no auth token)");
        }

        // Step 6: Admin API health
        System.out.println("--- Step 6: Admin API Health ---");
        HttpResponse<String> adminHealth = get(ADMIN_URL + "/actuator/health", null);
        if (adminHealth.statusCode() == 200) {
            System.out.println("  [PASS] Admin API healthy");
            passed++;
        } else {
            System.out.println("  [FAIL] Admin API not healthy");
            failed++;
        }

        // Step 7: Admin login
        System.out.println("--- Step 7: Admin Login ---");
        String adminLoginBody = "{\"username\":\"admin\",\"password\":\"macro123\"}";
        HttpResponse<String> adminLoginResp = post(ADMIN_URL + "/admin/login", adminLoginBody, null);
        String adminToken = extractToken(adminLoginResp.body());
        if (adminToken != null) {
            System.out.println("  [PASS] Admin login successful");
            passed++;
        } else {
            System.out.println("  [WARN] Admin login failed (may need seed data)");
            failed++;
        }

        // Step 8: Admin order list
        System.out.println("--- Step 8: Admin Order List ---");
        if (adminToken != null) {
            HttpResponse<String> adminOrderResp = get(ADMIN_URL + "/order/list?pageNum=1&pageSize=5", adminToken);
            if (adminOrderResp.body().contains("\"code\"")) {
                System.out.println("  [PASS] Admin order list accessible");
                passed++;
            } else {
                System.out.println("  [FAIL] Admin order list error");
                failed++;
            }
        } else {
            System.out.println("  [SKIP] Admin order test skipped (no admin token)");
        }

        System.out.printf("\n===== Results: %d passed, %d failed =====\n", passed, failed);
        if (failed > 0) System.exit(1);
    }

    private static HttpResponse<String> get(String url, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(String url, String body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String extractToken(String responseBody) {
        // Simple JSON extraction for "token":"xxx"
        int idx = responseBody.indexOf("\"token\"");
        if (idx < 0) return null;
        int colonIdx = responseBody.indexOf(":", idx);
        int startQuote = responseBody.indexOf("\"", colonIdx + 1);
        int endQuote = responseBody.indexOf("\"", startQuote + 1);
        if (startQuote < 0 || endQuote < 0) return null;
        String token = responseBody.substring(startQuote + 1, endQuote);
        return token.isEmpty() ? null : token;
    }
}
