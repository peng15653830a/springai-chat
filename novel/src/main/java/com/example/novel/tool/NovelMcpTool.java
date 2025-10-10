package com.example.novel.tool;

import com.example.novel.dto.request.McpExecuteRequest;
import com.example.novel.dto.response.McpExecuteResponse;
import com.example.novel.service.mcp.McpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NovelMcpTool {

  private final McpService mcpService;
  private final ObjectMapper objectMapper;

  @Tool(description = "执行 MCP 工具调用，参数需为 JSON")
  public String executeMcpTool(
      @ToolParam(description = "工具名称") String toolName,
      @ToolParam(description = "JSON 格式的工具参数") String parametersJson,
      ToolContext toolContext) {

    if (toolName == null || toolName.isBlank()) {
      return "未指定工具名称";
    }

    Map<String, Object> parameters = parseParameters(parametersJson);
    Map<String, Object> ctx =
        toolContext != null ? toolContext.getContext() : Collections.emptyMap();
    Long sessionId = asLong(ctx.get("conversationId"));
    Long messageId = asLong(ctx.get("messageId"));

    McpExecuteRequest request = new McpExecuteRequest();
    request.setToolName(toolName);
    request.setParameters(parameters);
    request.setSessionId(sessionId);
    request.setMessageId(messageId);

    try {
      McpExecuteResponse response =
          mcpService.executeTool(request).block(Duration.ofSeconds(8));
      if (response == null) {
        return "MCP调用返回为空";
      }
      if (Boolean.TRUE.equals(response.getSuccess())) {
        return serialize(response.getResult());
      }
      return response.getError() != null ? response.getError() : "MCP调用失败";
    } catch (Exception e) {
      log.warn("MCP调用失败: {}", e.getMessage());
      return "MCP调用异常: " + e.getMessage();
    }
  }

  private Map<String, Object> parseParameters(String json) {
    if (json == null || json.isBlank()) {
      return Collections.emptyMap();
    }
    try {
      return objectMapper.readValue(json, Map.class);
    } catch (Exception e) {
      log.warn("解析MCP工具参数失败: {}", e.getMessage());
      return Collections.emptyMap();
    }
  }

  private String serialize(Object result) {
    try {
      return objectMapper.writeValueAsString(result);
    } catch (Exception e) {
      return String.valueOf(result);
    }
  }

  private Long asLong(Object value) {
    if (value instanceof Long l) {
      return l;
    }
    if (value instanceof Number n) {
      return n.longValue();
    }
    try {
      return value != null ? Long.parseLong(String.valueOf(value)) : null;
    } catch (Exception ignore) {
      return null;
    }
  }
}
