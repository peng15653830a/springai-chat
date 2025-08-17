package com.example.service;

import com.example.entity.User;

/**
 * 用户服务接口
 *
 * @author xupeng
 */
public interface UserService {

  /**
   * 创建用户
   *
   * @param username 用户名
   * @param nickname 用户昵称
   * @return 用户实体
   */
  User createUser(String username, String nickname);

  /**
   * 根据ID获取用户
   *
   * @param userId 用户ID
   * @return 用户实体
   */
  User getUserById(Long userId);

  /**
   * 根据用户名获取用户
   *
   * @param username 用户名
   * @return 用户实体
   */
  User getUserByUsername(String username);

  /**
   * 登录或创建用户
   *
   * @param username 用户名
   * @param nickname 用户昵称
   * @return 用户实体
   */
  User loginOrCreate(String username, String nickname);

  /**
   * 更新用户
   *
   * @param user 用户实体
   */
  void updateUser(User user);
}
