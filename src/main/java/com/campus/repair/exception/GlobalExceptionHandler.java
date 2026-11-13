package com.campus.repair.exception;
import com.campus.repair.common.Result;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.stream.Collectors;
/**
 * 全局异常处理器。
 * <p>
 * 作用：拦截 Controller 层抛出的所有异常，统一转换成 {@link Result} 格式返回，
 * 避免把 Java 异常堆栈直接暴露给前端。
 * <p>
 * 说明：HTTP 状态码统一返回 200，业务是否成功由响应体里的 code 判断（前端按 code 分发）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * 参数校验失败：@RequestBody + @Valid 校验不通过。
     * 例如：提交报修单时 title 为空。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.fail(Result.CODE_BAD_REQUEST, message);
    }
    /**
     * 参数校验失败：表单 / 普通对象（非 @RequestBody）绑定校验不通过。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败：{}", message);
        return Result.fail(Result.CODE_BAD_REQUEST, message);
    }
    /**
     * 参数校验失败：@Validated 作用在 Controller 方法参数上时校验不通过。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.fail(Result.CODE_BAD_REQUEST, message);
    }
    /**
     * 缺少必填的请求参数，例如 ?page 没传。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = "缺少必填参数：" + e.getParameterName();
        log.warn(message);
        return Result.fail(Result.CODE_BAD_REQUEST, message);
    }
    /**
     * 请求体不是合法 JSON（格式错误 / 字段类型不匹配）。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.fail(Result.CODE_BAD_REQUEST, "请求体格式错误，请检查 JSON 内容");
    }
    /**
     * 请求方法不支持，例如用 GET 访问只接受 POST 的接口。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMethod());
        return Result.fail(Result.CODE_METHOD_NOT_ALLOWED, "不支持的请求方法：" + e.getMethod());
    }
    /**
     * 访问的接口 / 静态资源不存在（Spring Boot 3.2 起抛 NoResourceFoundException）。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("资源不存在：{}", e.getResourcePath());
        return Result.fail(Result.CODE_NOT_FOUND, "请求的接口不存在：" + e.getResourcePath());
    }
    /**
     * 业务代码里主动抛出的 IllegalArgumentException（例如状态流转非法）。
     * 注意：这类属于调用方传参问题，返回 400 而不是 500。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("请求参数非法：{}", e.getMessage());
        return Result.fail(Result.CODE_BAD_REQUEST, e.getMessage() == null ? "请求参数非法" : e.getMessage());
    }
    /**
     * 兜底：任何没有被上面处理的异常。
     * 这里必须记录 error 日志（含堆栈），否则线上问题无法排查。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(Result.CODE_ERROR, "系统繁忙，请稍后重试");
    }
    /* ---------- 私有工具方法 ---------- */
    /**
     * 把字段校验错误格式化成 "字段名: 提示" 的形式。
     */
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}