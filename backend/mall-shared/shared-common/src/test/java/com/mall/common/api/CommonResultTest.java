package com.mall.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonResult 单元测试
 */
class CommonResultTest {

    @Test
    void success_withData_shouldReturnCode200() {
        CommonResult<String> result = CommonResult.success("hello");
        assertEquals(200, result.getCode());
        assertEquals("hello", result.getData());
        assertEquals("操作成功", result.getMessage());
    }

    @Test
    void success_withDataAndMessage_shouldUseCustomMessage() {
        CommonResult<String> result = CommonResult.success("data", "自定义消息");
        assertEquals(200, result.getCode());
        assertEquals("data", result.getData());
        assertEquals("自定义消息", result.getMessage());
    }

    @Test
    void failed_withMessage_shouldReturnCode500() {
        CommonResult<Object> result = CommonResult.failed("操作失败");
        assertEquals(500, result.getCode());
        assertNull(result.getData());
        assertEquals("操作失败", result.getMessage());
    }

    @Test
    void failed_withErrorCode_shouldUseErrorCodeValue() {
        CommonResult<Object> result = CommonResult.failed(ResultCode.UNAUTHORIZED);
        assertEquals(401, result.getCode());
        assertEquals("暂未登录或 token 已过期", result.getMessage());
    }

    @Test
    void validateFailed_shouldReturnCode400() {
        CommonResult<Object> result = CommonResult.validateFailed("字段校验失败");
        assertEquals(400, result.getCode());
        assertEquals("字段校验失败", result.getMessage());
    }

    @Test
    void unauthorized_shouldReturnCode401() {
        CommonResult<Object> result = CommonResult.unauthorized("未授权");
        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
    }

    @Test
    void forbidden_shouldReturnCode403() {
        CommonResult<Object> result = CommonResult.forbidden("禁止访问");
        assertEquals(403, result.getCode());
        assertEquals("禁止访问", result.getMessage());
    }
}
