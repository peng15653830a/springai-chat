package com.example.dto;

import lombok.Data;

/**
 * 登录请求DTO，用于接收用户登录的请求参数
 *
 * @author xupeng
 */
@Data
public class LoginRequest {
  /** 用户名 */
  private String username;
  
  /** 用户昵称 */
  private String nickname;
}
