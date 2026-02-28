package com.mall.common.exception;

import com.mall.common.api.IErrorCode;
import lombok.Getter;

/**
 * 自定义 API 异常
 */
@Getter
public class ApiException extends RuntimeException {
    private final IErrorCode errorCode;

    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(IErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiException(String message) {
        super(message);
        this.errorCode = null;
    }
}
