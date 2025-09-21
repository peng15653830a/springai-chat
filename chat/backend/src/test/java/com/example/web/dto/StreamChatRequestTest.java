package com.example.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dto.request.StreamChatRequest;
import org.junit.jupiter.api.Test;

/**
 * StreamChatRequest DTO单元测试 测试用于Spring自动参数绑定的纯POJO对象
 *
 * @author xupeng
 */
class StreamChatRequestTest {

  @Test
  void shouldCreateStreamChatRequestWithDefaults() {
    // Given
    StreamChatRequest request = new StreamChatRequest();
    request.setConversationId(1L);

    // When & Then
    assertThat(request.getConversationId()).isEqualTo(1L);
    assertThat(request.isSearchEnabled()).isFalse(); // 默认为false
    assertThat(request.isDeepThinking()).isFalse(); // 默认为false
    assertThat(request.getMessage()).isNull();
    assertThat(request.getUserId()).isNull();
    assertThat(request.getProvider()).isNull();
    assertThat(request.getModel()).isNull();
  }

  @Test
  void shouldSetAndGetAllProperties() {
    // Given
    StreamChatRequest request = new StreamChatRequest();
    Long conversationId = 123L;
    String message = "Test message";
    boolean searchEnabled = true;
    boolean deepThinking = true;
    Long userId = 456L;
    String provider = "openai";
    String model = "gpt-4";

    // When
    request.setConversationId(conversationId);
    request.setMessage(message);
    request.setSearchEnabled(searchEnabled);
    request.setDeepThinking(deepThinking);
    request.setUserId(userId);
    request.setProvider(provider);
    request.setModel(model);

    // Then
    assertThat(request.getConversationId()).isEqualTo(conversationId);
    assertThat(request.getMessage()).isEqualTo(message);
    assertThat(request.isSearchEnabled()).isEqualTo(searchEnabled);
    assertThat(request.isDeepThinking()).isEqualTo(deepThinking);
    assertThat(request.getUserId()).isEqualTo(userId);
    assertThat(request.getProvider()).isEqualTo(provider);
    assertThat(request.getModel()).isEqualTo(model);
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    StreamChatRequest request = new StreamChatRequest();
    request.setConversationId(1L);
    // 其他字段保持默认null值

    // Then
    assertThat(request.getConversationId()).isEqualTo(1L);
    assertThat(request.getMessage()).isNull();
    assertThat(request.getUserId()).isNull();
    assertThat(request.getProvider()).isNull();
    assertThat(request.getModel()).isNull();
    // 布尔字段有默认值
    assertThat(request.isSearchEnabled()).isFalse();
    assertThat(request.isDeepThinking()).isFalse();
  }

  @Test
  void shouldSupportSpringParameterBinding() {
    // 这个测试验证对象结构适合Spring自动参数绑定
    StreamChatRequest request = new StreamChatRequest();

    // 模拟Spring参数绑定过程
    // 路径参数: /stream/{conversationId}
    request.setConversationId(999L);

    // 查询参数:
    // ?message=hello&searchEnabled=true&deepThinking=false&userId=123&provider=qwen&model=qwen-plus
    request.setMessage("hello");
    request.setSearchEnabled(true);
    request.setDeepThinking(false);
    request.setUserId(123L);
    request.setProvider("qwen");
    request.setModel("qwen-plus");

    // 验证所有字段都能正确设置
    assertThat(request.getConversationId()).isEqualTo(999L);
    assertThat(request.getMessage()).isEqualTo("hello");
    assertThat(request.isSearchEnabled()).isTrue();
    assertThat(request.isDeepThinking()).isFalse();
    assertThat(request.getUserId()).isEqualTo(123L);
    assertThat(request.getProvider()).isEqualTo("qwen");
    assertThat(request.getModel()).isEqualTo("qwen-plus");
  }
}
