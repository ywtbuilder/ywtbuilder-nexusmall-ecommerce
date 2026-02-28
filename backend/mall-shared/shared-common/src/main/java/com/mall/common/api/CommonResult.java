package com.mall.common.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用 API 返回对象
 */
@Data
@NoArgsConstructor
public class CommonResult<T> {
    private long code;
    private String message;
    private T data;

    private CommonResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果（自定义消息）
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果（指定错误码）
     */
    public static <T> CommonResult<T> failed(IErrorCode errorCode) {
        return new CommonResult<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回结果（指定错误码 + 自定义消息）
     */
    public static <T> CommonResult<T> failed(IErrorCode errorCode, String message) {
        return new CommonResult<>(errorCode.getCode(), message, null);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return new CommonResult<>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> CommonResult<T> unauthorized(String message) {
        return new CommonResult<>(ResultCode.UNAUTHORIZED.getCode(), message, null);
    }

    /**
     * 未授权返回结果
     */
    public static <T> CommonResult<T> forbidden(String message) {
        return new CommonResult<>(ResultCode.FORBIDDEN.getCode(), message, null);
    }
}
