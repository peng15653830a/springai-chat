package com.example.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常类，用于处理业务逻辑中的异常情况
 *
 * @author xupeng
 */
@Setter
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 异常代码
     */
    private String code;

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    /**
     * 构造函数
     *
     * @param code    异常代码
     * @param message 异常消息
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "BUSINESS_ERROR";
    }

    /**
     * 构造函数
     *
     * @param code    异常代码
     * @param message 异常消息
     * @param cause   异常原因
     */
    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
