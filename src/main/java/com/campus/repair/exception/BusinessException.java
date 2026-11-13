package com.campus.repair.exception;

import com.campus.repair.common.Result;

/**
 * 自定义业务异常类
 * 用于在业务逻辑不满足时抛出（如：分类名称重复）
 */
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = Result.CODE_BAD_REQUEST; // 默认使用 400 错误码
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}