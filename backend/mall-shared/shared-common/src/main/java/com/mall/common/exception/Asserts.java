package com.mall.common.exception;

import com.mall.common.api.IErrorCode;

/**
 * 断言工具，抛出 ApiException
 */
public class Asserts {
    private Asserts() {}

    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }

    public static void fail(IErrorCode errorCode, String message) {
        throw new ApiException(errorCode, message);
    }
}
