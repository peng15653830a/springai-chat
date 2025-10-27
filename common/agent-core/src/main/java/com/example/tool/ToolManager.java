package com.example.tool;

import com.example.stream.TextStreamRequest;
import java.util.List;

/**
 * 工具管理器接口
 * 负责根据请求上下文动态解析需要注入的工具
 */
public interface ToolManager {

  /**
   * 根据请求上下文解析需要的工具
   *
   * @param request 流式请求对象，包含searchEnabled、mcpEnabled等标志
   * @return 需要注入的工具对象列表
   */
  List<Object> resolveTools(TextStreamRequest request);
}
