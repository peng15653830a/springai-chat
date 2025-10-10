package com.example.mapper;

import com.example.entity.Message;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper {
  void insert(Message message);
  Message selectById(@Param("id") Long id);
  List<Message> selectByConversationId(@Param("conversationId") Long conversationId);
  List<Message> selectRecentMessages(
      @Param("conversationId") Long conversationId, @Param("limit") int limit);
  void deleteById(@Param("id") Long id);
  void deleteByConversationId(@Param("conversationId") Long conversationId);
  void updateById(Message message);
}

