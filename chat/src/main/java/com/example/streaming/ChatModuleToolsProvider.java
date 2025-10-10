package com.example.streaming;

import com.example.stream.TextStreamRequest;
import com.example.stream.springai.ToolsProvider;
import com.example.tool.WebSearchTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@org.springframework.context.annotation.Primary
@Component
@RequiredArgsConstructor
public class ChatModuleToolsProvider implements ToolsProvider {
  private final WebSearchTool webSearchTool;
  @Override
  public Object[] resolveTools(TextStreamRequest request) {
    if (request.isSearchEnabled()) {
      return new Object[] {webSearchTool};
    }
    return new Object[0];
  }
}
