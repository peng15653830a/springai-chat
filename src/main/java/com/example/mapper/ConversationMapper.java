package com.example.mapper;

import com.example.entity.Conversation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 对话Mapper接口，用于操作对话相关的数据库操作
 *
 * @author xupeng
 */
@Mapper
public interface ConversationMapper {

  /**
   * 插入新的对话记录
   *
   * @param conversation 对话实体
   */
  void insert(Conversation conversation);

  /**
   * 根据ID查询对话
   *
   * @param id 对话ID
   * @return 对话实体
   */
  Conversation selectById(@Param("id") Long id);

  /**
   * 根据用户ID查询对话列表
   *
   * @param userId 用户ID
   * @return 对话列表
   */
  List<Conversation> selectByUserId(@Param("userId") Long userId);

  /**
   * 根据用户ID查询最近的对话列表
   *
   * @param userId 用户ID
   * @param limit 限制数量
   * @return 对话列表
   */
  List<Conversation> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

  /**
   * 根据ID更新对话
   *
   * @param conversation 对话实体
   */
  void updateById(Conversation conversation);

  /**
   * 根据ID删除对话
   *
   * @param id 对话ID
   */
  void deleteById(@Param("id") Long id);
}
