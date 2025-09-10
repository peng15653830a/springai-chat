package com.example.mapper;

import com.example.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息Mapper接口，用于操作消息相关的数据库操作
 *
 * @author xupeng
 */
@Mapper
public interface MessageMapper {

  /**
   * 插入新的消息记录
   *
   * @param message 消息实体
   */
  void insert(Message message);

  /**
   * 根据ID查询消息
   *
   * @param id 消息ID
   * @return 消息实体
   */
  Message selectById(@Param("id") Long id);

  /**
   * 根据对话ID查询消息列表
   *
   * @param conversationId 对话ID
   * @return 消息列表
   */
  List<Message> selectByConversationId(@Param("conversationId") Long conversationId);

  /**
   * 根据对话ID查询最近的消息列表
   *
   * @param conversationId 对话ID
   * @param limit 限制数量
   * @return 消息列表
   */
  List<Message> selectRecentMessages(
      @Param("conversationId") Long conversationId, @Param("limit") int limit);

  /**
   * 根据ID删除消息
   *
   * @param id 消息ID
   */
  void deleteById(@Param("id") Long id);

  /**
   * 根据对话ID删除消息
   *
   * @param conversationId 对话ID
   */
  void deleteByConversationId(@Param("conversationId") Long conversationId);
}
