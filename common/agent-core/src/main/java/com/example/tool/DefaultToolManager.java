package com.example.tool;

import com.example.stream.TextStreamRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 默认的工具管理器实现
 * 
 * <p>自动发现所有带@Tool注解的bean，并根据请求上下文动态注入
 */
@Slf4j
@Component
public class DefaultToolManager implements ToolManager {

  private final Map<String, Object> availableTools;

  @Autowired
  public DefaultToolManager(@Autowired(required = false) List<Object> allBeans) {
    this.availableTools = new java.util.HashMap<>();
    
    if (allBeans != null) {
      for (Object bean : allBeans) {
        if (hasToolAnnotation(bean)) {
          String toolName = bean.getClass().getSimpleName();
          availableTools.put(toolName, bean);
          log.debug("注册工具: {}", toolName);
        }
      }
    }
    
    log.info("ToolManager初始化完成，已注册 {} 个工具", availableTools.size());
  }

  @Override
  public List<Object> resolveTools(TextStreamRequest request) {
    List<Object> tools = new ArrayList<>();

    if (request == null) {
      return tools;
    }

    if (request.isSearchEnabled()) {
      Object webSearchTool = availableTools.get("WebSearchTool");
      if (webSearchTool != null) {
        tools.add(webSearchTool);
        log.debug("注入 WebSearchTool");
      }
    }

    return tools;
  }

  private boolean hasToolAnnotation(Object bean) {
    Class<?> clazz = bean.getClass();
    
    if (clazz.getSimpleName().contains("CGLIB")) {
      clazz = clazz.getSuperclass();
    }
    
    return java.util.Arrays.stream(clazz.getMethods())
        .anyMatch(
            method ->
                method.isAnnotationPresent(org.springframework.ai.tool.annotation.Tool.class));
  }
}
