package com.example.service;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public User createUser(String username, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        userMapper.insert(user);
        return user;
    }
    
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    public User loginOrCreate(String username, String nickname) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            user = createUser(username, nickname);
        }
        return user;
    }
    
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
}