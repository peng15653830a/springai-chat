package com.example.novel.streaming;

import com.example.novel.tool.NovelMcpTool;
import com.example.novel.tool.NovelRagTool;
import com.example.stream.TextStreamRequest;
import com.example.stream.springai.ToolsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class NovelModuleToolsProvider implements ToolsProvider {

  private final NovelRagTool novelRagTool;
  private final NovelMcpTool novelMcpTool;

  @Override
  public Object[] resolveTools(TextStreamRequest request) {
    return new Object[] {novelRagTool, novelMcpTool};
  }
}
