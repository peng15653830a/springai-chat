package com.example.controller;

import com.example.dto.response.ApiResponse;
import com.example.dto.request.LoginRequest;
import com.example.entity.User;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器，处理用户相关的HTTP请求
 *
 * @author xupeng
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

  @Autowired private UserService userService;

  /**
   * 用户登录接口
   *
   * @param request 登录请求对象，包含用户名和昵称
   * @return 登录结果的ApiResponse
   */
  @PostMapping("/login")
  public ApiResponse<User> login(@RequestBody LoginRequest request) {
    log.info("用户登录请求，用户名: {}", request.getUsername());

    try {
      User user = userService.loginOrCreate(request.getUsername(), request.getNickname());
      log.info("用户登录成功，用户ID: {}", user.getId());
      return ApiResponse.success("登录成功", user);
    } catch (Exception e) {
      log.error("用户登录失败，用户名: {}", request.getUsername(), e);
      return ApiResponse.error("登录失败: " + e.getMessage());
    }
  }

  /**
   * 获取用户信息接口
   *
   * @param userId 用户ID
   * @return 用户信息的ApiResponse
   */
  @GetMapping("/profile/{userId}")
  public ApiResponse<User> getProfile(@PathVariable Long userId) {
    log.debug("获取用户信息请求，用户ID: {}", userId);

    try {
      User user = userService.getUserById(userId);
      if (user == null) {
        log.warn("用户不存在，用户ID: {}", userId);
        return ApiResponse.error("用户不存在");
      }
      return ApiResponse.success(user);
    } catch (Exception e) {
      log.error("获取用户信息失败，用户ID: {}", userId, e);
      return ApiResponse.error("获取用户信息失败: " + e.getMessage());
    }
  }
}
