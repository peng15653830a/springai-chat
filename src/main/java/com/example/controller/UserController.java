package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.LoginRequest;
import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.loginOrCreate(request.getUsername(), request.getNickname());
            return ApiResponse.success("登录成功", user);
        } catch (Exception e) {
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/profile/{userId}")
    public ApiResponse<User> getProfile(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }
}