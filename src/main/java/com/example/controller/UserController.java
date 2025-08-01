package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.LoginRequest;
import com.example.entity.User;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
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