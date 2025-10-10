package com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户模型偏好请求对象 用于封装saveUserModelPreference方法的参数
 *
 * @author xupeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModelPreferenceRequest {

  /** 用户ID */
  private Long userId;

  /** 模型提供者名称 */
  private String providerName;

  /** 模型名称 */
  private String modelName;

  /** 是否设为默认模型 */
  private boolean isDefault;
}
