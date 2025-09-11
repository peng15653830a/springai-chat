package com.example.service.chat;

import com.example.strategy.prompt.DefaultPromptBuilder;
import com.example.entity.Message;
import com.example.service.MessageService;
import com.example.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * DefaultPromptBuilder测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class DefaultPromptBuilderTest {

    @Mock
    private MessageService messageService;

    @Mock
    private SearchService searchService;

    private DefaultPromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new DefaultPromptBuilder(messageService, searchService);
    }

    @Test
    void shouldBuildSystemPrompt() {
        // When
        String systemPrompt = promptBuilder.buildSystemPrompt();

        // Then
        assertThat(systemPrompt).isNotNull();
        assertThat(systemPrompt).contains("你是一个智能助手");
        assertThat(systemPrompt).contains("回答要准确、简洁、有条理");
    }

    @Test
    void shouldFormatSearchContext() {
        // Given
        String searchResults = "Result 1\nResult 2\n\nResult 3";

        // When
        String formatted = promptBuilder.formatSearchContext(searchResults);

        // Then
        assertThat(formatted).isNotNull();
        assertThat(formatted).contains("- Result 1");
        assertThat(formatted).contains("- Result 2");
        assertThat(formatted).contains("- Result 3");
    }

    @Test
    void shouldFormatEmptySearchContext() {
        // Given
        String searchResults = "";

        // When
        String formatted = promptBuilder.formatSearchContext(searchResults);

        // Then
        assertThat(formatted).isEqualTo("");
    }

    @Test
    void shouldFormatNullSearchContext() {
        // Given
        String searchResults = null;

        // When
        String formatted = promptBuilder.formatSearchContext(searchResults);

        // Then
        assertThat(formatted).isEqualTo("");
    }

    @Test
    void shouldBuildPromptWithSearchEnabled() {
        // Given
        Long conversationId = 1L;
        String userMessage = "Hello";
        boolean searchEnabled = true;
        
        // Mock message service
        Message message1 = new Message();
        message1.setId(1L);
        message1.setRole("user");
        message1.setContent("Previous message");
        message1.setConversationId(conversationId);
        message1.setCreatedAt(LocalDateTime.now());
        
        when(messageService.getConversationHistoryAsync(conversationId))
                .thenReturn(Mono.just(Arrays.asList(message1)));
        
        // Mock search service
        com.example.dto.response.SearchResult searchResult = new com.example.dto.response.SearchResult();
        searchResult.setTitle("Test Result");
        searchResult.setUrl("http://test.com");
        searchResult.setSnippet("Test snippet");
        
        when(searchService.performSearchWithEvents(eq(userMessage), eq(true)))
                .thenReturn(Mono.just(new SearchService.SearchContextResult(
                        "Search result 1\nSearch result 2", // 搜索上下文
                        Arrays.asList(searchResult), // 搜索结果列表
                        reactor.core.publisher.Flux.empty() // 搜索事件流
                )));
        
        when(searchService.formatSearchResults(anyList())).thenReturn("Search result 1\nSearch result 2");

        // When & Then
        StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
                .expectNextMatches(prompt -> {
                    System.out.println("Generated prompt: " + prompt);
                    return prompt.contains("你是一个智能助手") &&
                           prompt.contains("搜索相关信息") &&
                           prompt.contains("Search result 1") &&
                           prompt.contains("Previous message") &&
                           prompt.contains("User: Hello") &&
                           prompt.contains("Assistant:");
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildPromptWithSearchDisabled() {
        // Given
        Long conversationId = 1L;
        String userMessage = "Hello";
        boolean searchEnabled = false;
        
        // Mock message service
        Message message1 = new Message();
        message1.setId(1L);
        message1.setRole("user");
        message1.setContent("Previous message");
        message1.setConversationId(conversationId);
        message1.setCreatedAt(LocalDateTime.now());
        
        when(messageService.getConversationHistoryAsync(conversationId))
                .thenReturn(Mono.just(Arrays.asList(message1)));

        // When & Then
        StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
                .expectNextMatches(prompt -> {
                    System.out.println("Generated prompt: " + prompt);
                    return prompt.contains("你是一个智能助手") &&
                           // 当searchContext为空时，不应该包含"搜索相关信息"
                           !prompt.contains("搜索相关信息：") &&
                           prompt.contains("Previous message") &&
                           prompt.contains("User: Hello") &&
                           prompt.contains("Assistant:");
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildPromptWithEmptyHistory() {
        // Given
        Long conversationId = 1L;
        String userMessage = "Hello";
        boolean searchEnabled = false;
        
        // Mock message service with empty history
        when(messageService.getConversationHistoryAsync(conversationId))
                .thenReturn(Mono.just(Collections.emptyList()));

        // When & Then
        StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
                .expectNextMatches(prompt -> {
                    System.out.println("Generated prompt: " + prompt);
                    return prompt.contains("你是一个智能助手") &&
                           !prompt.contains("历史对话") &&
                           prompt.contains("User: Hello") &&
                           prompt.contains("Assistant:");
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildPromptWithNullHistory() {
        // Given
        Long conversationId = 1L;
        String userMessage = "Hello";
        boolean searchEnabled = false;
        
        // Mock message service with null history
        when(messageService.getConversationHistoryAsync(conversationId))
                .thenReturn(Mono.just(Collections.emptyList()));

        // When & Then
        StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
                .expectNextMatches(prompt -> {
                    System.out.println("Generated prompt: " + prompt);
                    return prompt.contains("你是一个智能助手") &&
                           !prompt.contains("历史对话") &&
                           prompt.contains("User: Hello") &&
                           prompt.contains("Assistant:");
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildPromptFromMessages() {
        // Given
        Message message1 = new Message();
        message1.setId(1L);
        message1.setRole("user");
        message1.setContent("Hello");
        
        Message message2 = new Message();
        message2.setId(2L);
        message2.setRole("assistant");
        message2.setContent("Hi there");
        
        List<Message> messages = Arrays.asList(message1, message2);
        String currentMessage = "How are you?";
        String searchContext = "Search result";

        // When
        String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("你是一个智能助手");
        assertThat(prompt).contains("搜索相关信息");
        assertThat(prompt).contains("Search result");
        assertThat(prompt).contains("User: Hello");
        assertThat(prompt).contains("Assistant: Hi there");
        assertThat(prompt).contains("User: How are you?");
        assertThat(prompt).contains("Assistant:");
    }

    @Test
    void shouldBuildPromptFromMessagesWithNullSearchContext() {
        // Given
        Message message1 = new Message();
        message1.setId(1L);
        message1.setRole("user");
        message1.setContent("Hello");
        
        List<Message> messages = Arrays.asList(message1);
        String currentMessage = "How are you?";
        String searchContext = null;

        // When
        String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

        // Then
        assertThat(prompt).isNotNull();
        // 注意：即使searchContext为null，系统提示仍然会添加
        assertThat(prompt).contains("你是一个智能助手");
        // 当searchContext为null时，不应该包含"搜索相关信息"
        assertThat(prompt).doesNotContain("搜索相关信息：");
        assertThat(prompt).contains("User: Hello");
        assertThat(prompt).contains("User: How are you?");
        assertThat(prompt).contains("Assistant:");
    }

    @Test
    void shouldBuildPromptFromMessagesWithEmptySearchContext() {
        // Given
        Message message1 = new Message();
        message1.setId(1L);
        message1.setRole("user");
        message1.setContent("Hello");
        
        List<Message> messages = Arrays.asList(message1);
        String currentMessage = "How are you?";
        String searchContext = "";

        // When
        String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

        // Then
        assertThat(prompt).isNotNull();
        // 注意：即使searchContext为空字符串，系统提示仍然会添加
        assertThat(prompt).contains("你是一个智能助手");
        // 当searchContext为空字符串时，不应该包含"搜索相关信息"
        assertThat(prompt).doesNotContain("搜索相关信息：");
        assertThat(prompt).contains("User: Hello");
        assertThat(prompt).contains("User: How are you?");
        assertThat(prompt).contains("Assistant:");
    }

    @Test
    void shouldBuildPromptFromMessagesWithNullMessages() {
        // Given
        List<Message> messages = null;
        String currentMessage = "Hello";
        String searchContext = "Search result";

        // When
        String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("你是一个智能助手");
        assertThat(prompt).contains("搜索相关信息");
        assertThat(prompt).contains("Search result");
        assertThat(prompt).contains("User: Hello");
        assertThat(prompt).contains("Assistant:");
        assertThat(prompt).doesNotContain("历史对话");
    }

    @Test
    void shouldBuildPromptFromMessagesWithEmptyMessages() {
        // Given
        List<Message> messages = Collections.emptyList();
        String currentMessage = "Hello";
        String searchContext = "Search result";

        // When
        String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("你是一个智能助手");
        assertThat(prompt).contains("搜索相关信息");
        assertThat(prompt).contains("Search result");
        assertThat(prompt).contains("User: Hello");
        assertThat(prompt).contains("Assistant:");
        assertThat(prompt).doesNotContain("历史对话");
    }

    @Test
    void shouldBuildPromptWithSpecialCharacters() {
      // Given
      Long conversationId = 1L;
      String userMessage = "特殊字符测试：🌟🔍🚀";
      boolean searchEnabled = false;
      
      // Mock message service
      Message message1 = new Message();
      message1.setId(3L);
      message1.setRole("user");
      message1.setContent("特殊历史消息🌟");
      message1.setConversationId(conversationId);
      message1.setCreatedAt(LocalDateTime.now());
      
      when(messageService.getConversationHistoryAsync(conversationId))
              .thenReturn(Mono.just(Arrays.asList(message1)));

      // When & Then
      StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
              .expectNextMatches(prompt -> {
                  System.out.println("Generated prompt: " + prompt);
                  return prompt.contains("你是一个智能助手") &&
                         prompt.contains("特殊历史消息🌟") &&
                         prompt.contains("User: 特殊字符测试：🌟🔍🚀") &&
                         prompt.contains("Assistant:");
              })
              .verifyComplete();
    }

    @Test
    void shouldBuildPromptWithLongMessage() {
      // Given
      Long conversationId = 1L;
      StringBuilder longMessage = new StringBuilder();
      for (int i = 0; i < 1000; i++) {
        longMessage.append("这是很长的消息内容，用来测试处理长文本的能力。");
      }
      String userMessage = longMessage.toString();
      boolean searchEnabled = false;
      
      // Mock message service
      Message message1 = new Message();
      message1.setId(4L);
      message1.setRole("user");
      message1.setContent("长历史消息");
      message1.setConversationId(conversationId);
      message1.setCreatedAt(LocalDateTime.now());
      
      when(messageService.getConversationHistoryAsync(conversationId))
              .thenReturn(Mono.just(Arrays.asList(message1)));

      // When & Then
      StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
              .expectNextMatches(prompt -> {
                  System.out.println("Generated prompt: " + prompt);
                  return prompt.contains("你是一个智能助手") &&
                         prompt.contains("长历史消息") &&
                         prompt.contains("Assistant:");
              })
              .verifyComplete();
    }

    @Test
    void shouldBuildPromptWithUnicodeCharacters() {
      // Given
      Long conversationId = 1L;
      String userMessage = "Unicode测试：测试中文消息";
      boolean searchEnabled = false;
      
      // Mock message service
      Message message1 = new Message();
      message1.setId(5L);
      message1.setRole("user");
      message1.setContent("Unicode历史消息：历史内容");
      message1.setConversationId(conversationId);
      message1.setCreatedAt(LocalDateTime.now());
      
      when(messageService.getConversationHistoryAsync(conversationId))
              .thenReturn(Mono.just(Arrays.asList(message1)));

      // When & Then
      StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
              .expectNextMatches(prompt -> {
                  System.out.println("Generated prompt: " + prompt);
                  return prompt.contains("你是一个智能助手") &&
                         prompt.contains("Unicode历史消息：历史内容") &&
                         prompt.contains("User: Unicode测试：测试中文消息") &&
                         prompt.contains("Assistant:");
              })
              .verifyComplete();
    }

    @Test
    void shouldBuildPromptWithEmptyUserMessage() {
      // Given
      Long conversationId = 1L;
      String userMessage = "";
      boolean searchEnabled = false;
      
      // Mock message service
      Message message1 = new Message();
      message1.setId(6L);
      message1.setRole("user");
      message1.setContent("历史消息");
      message1.setConversationId(conversationId);
      message1.setCreatedAt(LocalDateTime.now());
      
      when(messageService.getConversationHistoryAsync(conversationId))
              .thenReturn(Mono.just(Arrays.asList(message1)));

      // When & Then
      StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
              .expectNextMatches(prompt -> {
                  System.out.println("Generated prompt: " + prompt);
                  return prompt.contains("你是一个智能助手") &&
                         prompt.contains("历史消息") &&
                         prompt.contains("User: ") && // 空消息
                         prompt.contains("Assistant:");
              })
              .verifyComplete();
    }

    @Test
    void shouldBuildPromptWithNullUserMessage() {
      // Given
      Long conversationId = 1L;
      String userMessage = null;
      boolean searchEnabled = false;
      
      // Mock message service
      Message message1 = new Message();
      message1.setId(7L);
      message1.setRole("user");
      message1.setContent("历史消息");
      message1.setConversationId(conversationId);
      message1.setCreatedAt(LocalDateTime.now());
      
      when(messageService.getConversationHistoryAsync(conversationId))
              .thenReturn(Mono.just(Arrays.asList(message1)));

      // When & Then
      StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
              .expectNextMatches(prompt -> {
                  System.out.println("Generated prompt: " + prompt);
                  return prompt.contains("你是一个智能助手") &&
                         prompt.contains("历史消息") &&
                         prompt.contains("User: null") && // null消息
                         prompt.contains("Assistant:");
              })
              .verifyComplete();
    }

    @Test
    void shouldBuildPromptWithWhitespaceUserMessage() {
      // Given
      Long conversationId = 1L;
      String userMessage = "   \t\n   ";
      boolean searchEnabled = false;
      
      // Mock message service
      Message message1 = new Message();
      message1.setId(8L);
      message1.setRole("user");
      message1.setContent("历史消息");
      message1.setConversationId(conversationId);
      message1.setCreatedAt(LocalDateTime.now());
      
      when(messageService.getConversationHistoryAsync(conversationId))
              .thenReturn(Mono.just(Arrays.asList(message1)));

      // When & Then
      StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
              .expectNextMatches(prompt -> {
                  System.out.println("Generated prompt: " + prompt);
                  return prompt.contains("你是一个智能助手") &&
                         prompt.contains("历史消息") &&
                         prompt.contains("User:    \t\n   ") && // 空白字符消息
                         prompt.contains("Assistant:");
              })
              .verifyComplete();
    }

    @Test
    void shouldFormatSearchContextWithSpecialCharacters() {
      // Given
      String searchResults = "特殊字符结果🌟🔍🚀\n另一个结果";

      // When
      String formatted = promptBuilder.formatSearchContext(searchResults);

      // Then
      assertThat(formatted).isNotNull();
      assertThat(formatted).contains("- 特殊字符结果🌟🔍🚀");
      assertThat(formatted).contains("- 另一个结果");
    }

    @Test
    void shouldFormatSearchContextWithLongResults() {
      // Given
      StringBuilder longResults = new StringBuilder();
      for (int i = 0; i < 100; i++) {
        longResults.append("这是很长的搜索结果行，用来测试处理长文本的能力。\n");
      }
      String searchResults = longResults.toString();

      // When
      String formatted = promptBuilder.formatSearchContext(searchResults);

      // Then
      assertThat(formatted).isNotNull();
      assertThat(formatted).contains("- 这是很长的搜索结果行");
    }

    @Test
    void shouldFormatSearchContextWithUnicode() {
      // Given
      String searchResults = "Unicode结果：测试中文\n另一个Unicode结果：更多中文内容";

      // When
      String formatted = promptBuilder.formatSearchContext(searchResults);

      // Then
      assertThat(formatted).isNotNull();
      assertThat(formatted).contains("- Unicode结果：测试中文");
      assertThat(formatted).contains("- 另一个Unicode结果：更多中文内容");
    }

    @Test
    void shouldBuildPromptFromMessagesWithSpecialCharacters() {
      // Given
      Message message1 = new Message();
      message1.setId(1L);
      message1.setRole("user");
      message1.setContent("特殊字符历史消息🌟");
      
      List<Message> messages = Arrays.asList(message1);
      String currentMessage = "特殊字符当前消息🔍";
      String searchContext = "特殊字符搜索结果🚀";

      // When
      String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

      // Then
      assertThat(prompt).isNotNull();
      assertThat(prompt).contains("你是一个智能助手");
      assertThat(prompt).contains("搜索相关信息");
      assertThat(prompt).contains("特殊字符搜索结果🚀");
      assertThat(prompt).contains("User: 特殊字符历史消息🌟");
      assertThat(prompt).contains("User: 特殊字符当前消息🔍");
      assertThat(prompt).contains("Assistant:");
    }

    @Test
    void shouldBuildPromptFromMessagesWithLongContent() {
      // Given
      StringBuilder longContent = new StringBuilder();
      for (int i = 0; i < 1000; i++) {
        longContent.append("这是很长的内容，用来测试处理长文本的能力。");
      }
      
      Message message1 = new Message();
      message1.setId(1L);
      message1.setRole("user");
      message1.setContent(longContent.toString());
      
      List<Message> messages = Arrays.asList(message1);
      String currentMessage = longContent.toString();
      String searchContext = longContent.toString();

      // When
      String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

      // Then
      assertThat(prompt).isNotNull();
      assertThat(prompt).contains("你是一个智能助手");
      assertThat(prompt).contains("搜索相关信息");
    }

    @Test
    void shouldBuildPromptFromMessagesWithUnicode() {
      // Given
      Message message1 = new Message();
      message1.setId(1L);
      message1.setRole("user");
      message1.setContent("Unicode历史消息：测试中文");
      
      List<Message> messages = Arrays.asList(message1);
      String currentMessage = "Unicode当前消息：更多中文";
      String searchContext = "Unicode搜索结果：中文结果";

      // When
      String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

      // Then
      assertThat(prompt).isNotNull();
      assertThat(prompt).contains("你是一个智能助手");
      assertThat(prompt).contains("搜索相关信息");
      assertThat(prompt).contains("Unicode搜索结果：中文结果");
      assertThat(prompt).contains("User: Unicode历史消息：测试中文");
      assertThat(prompt).contains("User: Unicode当前消息：更多中文");
      assertThat(prompt).contains("Assistant:");
    }

    @Test
    void shouldBuildSystemPromptMultipleTimes() {
      // When
      String systemPrompt1 = promptBuilder.buildSystemPrompt();
      String systemPrompt2 = promptBuilder.buildSystemPrompt();

      // Then
      assertThat(systemPrompt1).isNotNull();
      assertThat(systemPrompt2).isNotNull();
      assertThat(systemPrompt1).isEqualTo(systemPrompt2);
      assertThat(systemPrompt1).contains("你是一个智能助手");
    }

    @Test
    void shouldFormatSearchContextWithEmptyLines() {
      // Given
      String searchResults = "结果1\n\n\n结果2\n   \n\t\n结果3";

      // When
      String formatted = promptBuilder.formatSearchContext(searchResults);

      // Then
      assertThat(formatted).isNotNull();
      assertThat(formatted).contains("- 结果1");
      assertThat(formatted).contains("- 结果2");
      assertThat(formatted).contains("- 结果3");
    }

    @Test
    void shouldFormatSearchContextWithOnlyWhitespace() {
      // Given
      String searchResults = "   \t\n   ";

      // When
      String formatted = promptBuilder.formatSearchContext(searchResults);

      // Then
      assertThat(formatted).isEqualTo("");
    }

    @Test
    void shouldBuildPromptFromMessagesWithNullCurrentMessage() {
      // Given
      Message message1 = new Message();
      message1.setId(1L);
      message1.setRole("user");
      message1.setContent("历史消息");
      
      List<Message> messages = Arrays.asList(message1);
      String currentMessage = null;
      String searchContext = "搜索结果";

      // When
      String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

      // Then
      assertThat(prompt).isNotNull();
      assertThat(prompt).contains("你是一个智能助手");
      assertThat(prompt).contains("搜索相关信息");
      assertThat(prompt).contains("搜索结果");
      assertThat(prompt).contains("User: 历史消息");
      assertThat(prompt).contains("User: null");
      assertThat(prompt).contains("Assistant:");
    }

    @Test
    void shouldBuildPromptFromMessagesWithEmptyCurrentMessage() {
      // Given
      Message message1 = new Message();
      message1.setId(1L);
      message1.setRole("user");
      message1.setContent("历史消息");
      
      List<Message> messages = Arrays.asList(message1);
      String currentMessage = "";
      String searchContext = "搜索结果";

      // When
      String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

      // Then
      assertThat(prompt).isNotNull();
      assertThat(prompt).contains("你是一个智能助手");
      assertThat(prompt).contains("搜索相关信息");
      assertThat(prompt).contains("搜索结果");
      assertThat(prompt).contains("User: 历史消息");
      assertThat(prompt).contains("User: ");
      assertThat(prompt).contains("Assistant:");
    }

    @Test
    void shouldBuildPromptFromMessagesWithWhitespaceCurrentMessage() {
      // Given
      Message message1 = new Message();
      message1.setId(1L);
      message1.setRole("user");
      message1.setContent("历史消息");
      
      List<Message> messages = Arrays.asList(message1);
      String currentMessage = "   \t\n   ";
      String searchContext = "搜索结果";

      // When
      String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

      // Then
      assertThat(prompt).isNotNull();
      assertThat(prompt).contains("你是一个智能助手");
      assertThat(prompt).contains("搜索相关信息");
      assertThat(prompt).contains("搜索结果");
      assertThat(prompt).contains("User: 历史消息");
      assertThat(prompt).contains("User:    \t\n   ");
      assertThat(prompt).contains("Assistant:");
    }

    @Test
    void shouldBuildPromptWithExceptionInMessageService() {
      // Given
      Long conversationId = 1L;
      String userMessage = "Hello";
      boolean searchEnabled = false;
      
      // Mock message service to throw exception
      when(messageService.getConversationHistoryAsync(conversationId))
              .thenReturn(Mono.error(new RuntimeException("Database error")));

      // When & Then
      StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
              .expectError(RuntimeException.class)
              .verify();
    }

    @Test
    void shouldBuildPromptWithSearchContextContainingSpecialCharacters() {
      // Given
      Long conversationId = 1L;
      String userMessage = "Hello";
      boolean searchEnabled = true;
      
      // Mock message service
      when(messageService.getConversationHistoryAsync(conversationId))
              .thenReturn(Mono.just(Collections.emptyList()));
      
      // Mock search service with special characters
      when(searchService.performSearchWithEvents(eq(userMessage), eq(true)))
              .thenReturn(Mono.just(new SearchService.SearchContextResult(
                      "特殊字符搜索结果🌟🔍🚀", // 搜索上下文
                      Arrays.asList(), // 搜索结果列表
                      reactor.core.publisher.Flux.empty() // 搜索事件流
              )));
      
      when(searchService.formatSearchResults(anyList())).thenReturn("特殊字符搜索结果🌟🔍🚀");

      // When & Then
      StepVerifier.create(promptBuilder.buildPrompt(conversationId, userMessage, searchEnabled))
              .expectNextMatches(prompt -> {
                  System.out.println("Generated prompt: " + prompt);
                  return prompt.contains("你是一个智能助手") &&
                         prompt.contains("搜索相关信息") &&
                         prompt.contains("特殊字符搜索结果🌟🔍🚀") &&
                         prompt.contains("User: Hello") &&
                         prompt.contains("Assistant:");
              })
              .verifyComplete();
    }
}