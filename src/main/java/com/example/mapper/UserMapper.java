package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口，用于操作用户相关的数据库操作
 *
 * @author xupeng
 */
@Mapper
public interface UserMapper {

  /**
   * 插入新的用户记录
   *
   * @param user 用户实体
   */
  void insert(User user);

  /**
   * 根据ID查询用户
   *
   * @param id 用户ID
   * @return 用户实体
   */
  User selectById(@Param("id") Long id);

  /**
   * 根据用户名查询用户
   *
   * @param username 用户名
   * @return 用户实体
   */
  User selectByUsername(@Param("username") String username);

  /**
   * 根据ID更新用户
   *
   * @param user 用户实体
   */
  void updateById(User user);

  /**
   * 根据ID删除用户
   *
   * @param id 用户ID
   */
  void deleteById(@Param("id") Long id);
}
