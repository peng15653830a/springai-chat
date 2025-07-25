package com.example.mapper;

import com.example.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MessageMapper {
    
    void insert(Message message);
    
    Message selectById(@Param("id") Long id);
    
    List<Message> selectByConversationId(@Param("conversationId") Long conversationId);
    
    List<Message> selectRecentMessages(@Param("conversationId") Long conversationId, @Param("limit") int limit);
    
    void deleteById(@Param("id") Long id);
    
    void deleteByConversationId(@Param("conversationId") Long conversationId);
}