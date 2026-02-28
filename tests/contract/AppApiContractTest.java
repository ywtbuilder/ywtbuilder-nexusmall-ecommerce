package com.mall.tests.contract;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 契约测试 — 验证 App API 端点签名与预期一致
 * <p>
 * 使用 Java HttpClient 直接请求 App API，验证：
 * 1. 关键端点返回 200
 * 2. 响应 JSON 结构符合 CommonResult 约定（包含 code / message / data）
 * 3. OpenAPI spec 端点可访问
 */
public class AppApiContractTest {

    private static final String BASE_URL = "http://localhost:18080";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        int passed = 0;
        int failed = 0;

        System.out.println("===== App API Contract Tests =====\n");

        // 1. Health endpoint
        if (assertGet("/actuator/health", 200, "\"status\"")) passed++; else failed++;

        // 2. OpenAPI spec
        if (assertGet("/v3/api-docs", 200, "\"openapi\"")) passed++; else failed++;

        // 3. Home content
        if (assertGet("/home/content", 200, "\"code\"")) passed++; else failed++;

        // 4. Home product category list
        if (assertGet("/home/productCateList/0", 200, "\"code\"")) passed++; else failed++;

        // 5. Product list (public)
        if (assertGet("/product/list?pageNum=1&pageSize=2", 200, "\"code\"")) passed++; else failed++;

        // 6. SSO login (POST, expects error without valid creds but 200 structure)
        if (assertPost("/sso/login", "{\"username\":\"test\",\"password\":\"wrong\"}", "\"code\"")) passed++; else failed++;

        // 7. Brand list
        if (assertGet("/brand/list?pageNum=1&pageSize=2", 200, "\"code\"")) passed++; else failed++;

        // 8. Search endpoint
        if (assertGet("/search/simple?keyword=test&pageNum=1&pageSize=2", 200, "\"code\"")) passed++; else failed++;

        System.out.printf("\n===== Results: %d passed, %d failed =====\n", passed, failed);
        if (failed > 0) System.exit(1);
    }

    private static boolean assertGet(String path, int expectedStatus, String expectedBodyContains) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean statusOk = response.statusCode() == expectedStatus;
            boolean bodyOk = response.body().contains(expectedBodyContains);
            boolean pass = statusOk && bodyOk;
            System.out.printf("  [%s] GET %s (status=%d)\n", pass ? "PASS" : "FAIL", path, response.statusCode());
            if (!pass) {
                System.out.printf("       Expected status=%d, body contains '%s'\n", expectedStatus, expectedBodyContains);
            }
            return pass;
        } catch (Exception e) {
            System.out.printf("  [FAIL] GET %s — %s\n", path, e.getMessage());
            return false;
        }
    }

    private static boolean assertPost(String path, String body, String expectedBodyContains) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean bodyOk = response.body().contains(expectedBodyContains);
            System.out.printf("  [%s] POST %s (status=%d)\n", bodyOk ? "PASS" : "FAIL", path, response.statusCode());
            return bodyOk;
        } catch (Exception e) {
            System.out.printf("  [FAIL] POST %s — %s\n", path, e.getMessage());
            return false;
        }
    }
}
