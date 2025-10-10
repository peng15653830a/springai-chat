package com.example.novel.mapper;

import com.example.novel.entity.NovelMessage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NovelMessageMapper {
  void insert(NovelMessage msg);
  List<NovelMessage> selectBySessionId(@Param("sessionId") Long sessionId);
  List<NovelMessage> selectBySessionIdWithLimit(@Param("sessionId") Long sessionId, @Param("limit") int limit);

  // 新增方法支持ChatMemory
  List<NovelMessage> findBySessionId(@Param("sessionId") Long sessionId);
  void deleteBySessionId(@Param("sessionId") Long sessionId);
}
