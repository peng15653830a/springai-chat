package com.example.exception;

/**
 * 业务异常类，用于处理业务逻辑中的异常情况
 *
 * @author xupeng
 */
public class BusinessException extends RuntimeException {

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
   * @param code 异常代码
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
   * @param cause 异常原因
   */
  public BusinessException(String message, Throwable cause) {
    super(message, cause);
    this.code = "BUSINESS_ERROR";
  }

  /**
   * 构造函数
   *
   * @param code 异常代码
   * @param message 异常消息
   * @param cause 异常原因
   */
  public BusinessException(String code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  /**
   * 获取异常代码
   *
   * @return 异常代码
   */
  public String getCode() {
    return code;
  }

  /**
   * 设置异常代码
   *
   * @param code 异常代码
   */
  public void setCode(String code) {
    this.code = code;
  }
}
