package com.example.stream.springai;

import com.example.stream.TextStreamRequest;

/**
 * 提供按需注入的工具（用于 Spring AI Tool Calling）。
 */
public interface ToolsProvider {
  /**
   * 返回要注入的工具对象数组（可为空数组）。
   */
  Object[] resolveTools(TextStreamRequest request);
}

