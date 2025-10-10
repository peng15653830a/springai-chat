package com.example.novel.mapper;

import com.example.novel.entity.NovelReference;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NovelReferenceMapper {
  void insert(NovelReference ref);
  List<NovelReference> selectBySessionId(@Param("sessionId") Long sessionId);
  List<NovelReference> selectByMessageId(@Param("messageId") Long messageId);
}

