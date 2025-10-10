package com.example.stream.springai;

import com.example.stream.TextStreamRequest;
import org.springframework.stereotype.Component;

@Component
public class NoOpToolsProvider implements ToolsProvider {
  @Override
  public Object[] resolveTools(TextStreamRequest request) { return new Object[0]; }
}

