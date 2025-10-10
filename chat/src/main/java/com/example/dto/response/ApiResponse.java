package com.example.dto.response;

import lombok.Data;

/**
 * API响应结果封装类
 *
 * @author xupeng
 * @param <T> 响应数据类型
 */
@Data
public class ApiResponse<T> {
  /** 是否成功 */
  private boolean success;

  /** 响应消息 */
  private String message;

  /** 响应数据 */
  private T data;

  /**
   * 创建成功的响应结果
   *
   * @param data 响应数据
   * @param <T> 数据类型
   * @return ApiResponse实例
   */
  public static <T> ApiResponse<T> success(T data) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(true);
    response.setData(data);
    return response;
  }

  /**
   * 创建成功的响应结果
   *
   * @param message 响应消息
   * @param data 响应数据
   * @param <T> 数据类型
   * @return ApiResponse实例
   */
  public static <T> ApiResponse<T> success(String message, T data) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(true);
    response.setMessage(message);
    response.setData(data);
    return response;
  }

  /**
   * 创建错误的响应结果
   *
   * @param message 错误消息
   * @param <T> 数据类型
   * @return ApiResponse实例
   */
  public static <T> ApiResponse<T> error(String message) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(false);
    response.setMessage(message);
    return response;
  }

  /**
   * 创建错误的响应结果
   *
   * @param code 错误代码
   * @param message 错误消息
   * @param <T> 数据类型
   * @return ApiResponse实例
   */
  public static <T> ApiResponse<T> error(String code, String message) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(false);
    response.setMessage(message);
    return response;
  }

  /**
   * 创建错误的响应结果
   *
   * @param code 错误代码
   * @param message 错误消息
   * @param data 响应数据
   * @param <T> 数据类型
   * @return ApiResponse实例
   */
  public static <T> ApiResponse<T> error(String code, String message, T data) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(false);
    response.setMessage(message);
    response.setData(data);
    return response;
  }
}
