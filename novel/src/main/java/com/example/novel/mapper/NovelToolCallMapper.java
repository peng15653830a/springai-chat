package com.example.novel.mapper;

import com.example.novel.entity.NovelToolCall;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NovelToolCallMapper {
  void insert(NovelToolCall call);
  void update(NovelToolCall call);
  List<NovelToolCall> selectBySessionId(@Param("sessionId") Long sessionId);
  List<NovelToolCall> selectByMessageId(@Param("messageId") Long messageId);
}

