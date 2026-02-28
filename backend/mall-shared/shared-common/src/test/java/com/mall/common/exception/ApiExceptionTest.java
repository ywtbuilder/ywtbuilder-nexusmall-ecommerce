package com.mall.common.exception;

import com.mall.common.api.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiException + Asserts 单元测试
 */
class ApiExceptionTest {

    @Test
    void apiException_withMessage_shouldHaveNullErrorCode() {
        ApiException ex = new ApiException("自定义错误");
        assertEquals("自定义错误", ex.getMessage());
        assertNull(ex.getErrorCode());
    }

    @Test
    void apiException_withErrorCode_shouldUseErrorCodeMessage() {
        ApiException ex = new ApiException(ResultCode.UNAUTHORIZED);
        assertEquals(ResultCode.UNAUTHORIZED, ex.getErrorCode());
        assertEquals("暂未登录或 token 已过期", ex.getMessage());
    }

    @Test
    void apiException_withErrorCodeAndMessage_shouldUseCustomMessage() {
        ApiException ex = new ApiException(ResultCode.FAILED, "自定义消息");
        assertEquals(ResultCode.FAILED, ex.getErrorCode());
        assertEquals("自定义消息", ex.getMessage());
    }

    @Test
    void asserts_failWithMessage_shouldThrowApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> Asserts.fail("boom"));
        assertEquals("boom", ex.getMessage());
    }

    @Test
    void asserts_failWithErrorCode_shouldThrowApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> Asserts.fail(ResultCode.FORBIDDEN));
        assertEquals(ResultCode.FORBIDDEN, ex.getErrorCode());
    }
}
