package com.example.novel.mapper;

import com.example.novel.entity.NovelSession;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NovelSessionMapper {
  void insert(NovelSession session);
  void update(NovelSession session);
  NovelSession selectById(@Param("id") Long id);
  List<NovelSession> selectRecent(@Param("limit") int limit);
}

