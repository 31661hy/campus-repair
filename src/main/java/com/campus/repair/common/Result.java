package com.campus.repair.common;
import java.io.Serializable;
/**
 * 统一响应结果（整个项目所有接口都返回这个格式）。
 * <p>
 * 返回结构：
 * <pre>
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... },          // 泛型 T，任何业务数据都放这里
 *   "timestamp": 1762912345678
 * }
 * </pre>
 *
 * @param <T> 业务数据类型
 */
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /* ---------- 业务状态码常量 ---------- */
    public static final int CODE_SUCCESS = 200;              // 成功
    public static final int CODE_BAD_REQUEST = 400;          // 参数错误
    public static final int CODE_UNAUTHORIZED = 401;         // 未登录 / 登录失效
    public static final int CODE_FORBIDDEN = 403;            // 无权限
    public static final int CODE_NOT_FOUND = 404;            // 资源不存在
    public static final int CODE_METHOD_NOT_ALLOWED = 405;   // 请求方法不支持
    public static final int CODE_ERROR = 500;                // 服务端异常
    private int code;         // 业务状态码
    private String message;   // 提示信息
    private T data;           // 业务数据
    private long timestamp;   // 响应时间戳（毫秒）
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    /* ==================== 成功 ==================== */
    /** 成功，无数据 */
    public static <T> Result<T> ok() {
        return ok(null);
    }
    /** 成功，带数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(CODE_SUCCESS, "操作成功", data);
    }
    /** 成功，自定义提示 + 数据 */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(CODE_SUCCESS, message == null ? "操作成功" : message, data);
    }
    /* ==================== 失败 ==================== */
    /** 失败，默认 500 */
    public static <T> Result<T> fail() {
        return fail(CODE_ERROR, "操作失败");
    }
    /** 失败，自定义提示（500） */
    public static <T> Result<T> fail(String message) {
        return new Result<>(CODE_ERROR, message, null);
    }
    /** 失败，自定义状态码 + 提示 */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
    /** 失败，自定义状态码 + 提示 + 数据（例如返回校验失败的字段明细） */
    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
    /* ==================== 工具方法 ==================== */
    /** 是否成功，前端/调用方常用来判断 */
    public boolean isSuccess() {
        return this.code == CODE_SUCCESS;
    }
    /* ==================== getter / setter ==================== */
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    @Override
    public String toString() {
        return "Result{code=" + code + ", message='" + message + "', data=" + data + ", timestamp=" + timestamp + '}';
    }
}