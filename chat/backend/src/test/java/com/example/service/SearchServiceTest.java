package com.example.service;

import com.example.config.SearchProperties;
import com.example.dto.response.SearchResult;
import com.example.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 移除SpringBootTest相关注解，改为纯Mockito测试
@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

  @Mock
  private SearchProperties searchProperties;

  @Mock
  private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

  private SearchService searchService;

  @BeforeEach
  void setUp() {
    // 创建SearchServiceImpl实例，使用mock的依赖
    searchService = new SearchServiceImpl(searchProperties, objectMapper);
  }

}