package com.mall.web.exception;

import com.mall.common.api.CommonResult;
import com.mall.common.api.ResultCode;
import com.mall.common.exception.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 — 统一 JSON 格式返回
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 业务异常 ----------

    @ExceptionHandler(ApiException.class)
    public CommonResult<Object> handleApiException(ApiException ex) {
        if (ex.getErrorCode() != null) {
            return CommonResult.failed(ex.getErrorCode(), ex.getMessage());
        }
        return CommonResult.failed(ex.getMessage());
    }

    // ---------- 参数校验异常 ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Object> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return CommonResult.validateFailed(String.join("; ", errors));
    }

    @ExceptionHandler(BindException.class)
    public CommonResult<Object> handleBindException(BindException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return CommonResult.validateFailed(String.join("; ", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<Object> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return CommonResult.validateFailed(message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public CommonResult<Object> handleMissingParam(MissingServletRequestParameterException ex) {
        return CommonResult.validateFailed("缺少必要参数: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CommonResult<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return CommonResult.validateFailed("参数类型错误: " + ex.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<Object> handleNotReadableException(HttpMessageNotReadableException ex) {
        return CommonResult.validateFailed("请求体格式错误");
    }

    // ---------- 请求方法 / 路径错误 ----------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResult<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return CommonResult.failed("不支持的请求方法: " + ex.getMethod());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public CommonResult<Object> handleNoResource(NoResourceFoundException ex) {
        return CommonResult.failed(ResultCode.FAILED, "资源不存在");
    }

    // ---------- 通用异常 ----------

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return CommonResult.failed(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<Object> handleUnhandled(Exception ex) {
        log.error("Unhandled exception", ex);
        return CommonResult.failed(ResultCode.FAILED, "系统内部错误");
    }

    private String toFieldError(FieldError error) {
        return error.getField() + ": " +
               (error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage());
    }
}
