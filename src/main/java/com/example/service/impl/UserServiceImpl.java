package com.example.service.impl;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author xupeng
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;

  @Override
  public User createUser(String username, String nickname) {
    User user = new User();
    user.setUsername(username);
    user.setNickname(nickname);
    userMapper.insert(user);
    return user;
  }

  @Override
  public User getUserById(Long userId) {
    return userMapper.selectById(userId);
  }

  @Override
  public User getUserByUsername(String username) {
    return userMapper.selectByUsername(username);
  }

  @Override
  public User loginOrCreate(String username, String nickname) {
    User user = userMapper.selectByUsername(username);
    if (user == null) {
      user = createUser(username, nickname);
    }
    return user;
  }

  @Override
  public void updateUser(User user) {
    userMapper.updateById(user);
  }
}
