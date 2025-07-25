package com.example.mapper;

import com.example.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ConversationMapper {
    
    void insert(Conversation conversation);
    
    Conversation selectById(@Param("id") Long id);
    
    List<Conversation> selectByUserId(@Param("userId") Long userId);
    
    List<Conversation> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    void updateById(Conversation conversation);
    
    void deleteById(@Param("id") Long id);
}