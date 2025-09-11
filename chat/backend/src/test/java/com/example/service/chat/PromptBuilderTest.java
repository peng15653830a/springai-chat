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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 提示词构建服务接口单元测试
 */
@ExtendWith(MockitoExtension.class)
class PromptBuilderTest {

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
    void testBuildSystemPrompt() {
        // 执行测试
        String systemPrompt = promptBuilder.buildSystemPrompt();

        // 验证结果
        assertThat(systemPrompt).isNotNull();
        assertThat(systemPrompt).contains("你是一个智能助手");
        assertThat(systemPrompt).contains("回答要准确、简洁、有条理");
    }

    @Test
    void testBuildPromptFromMessages() {
        // 准备测试数据
        Message userMessage = new Message();
        userMessage.setRole("user");
        userMessage.setContent("Hello");
        userMessage.setCreatedAt(LocalDateTime.now());

        Message assistantMessage = new Message();
        assistantMessage.setRole("assistant");
        assistantMessage.setContent("Hello! How can I help you?");
        assistantMessage.setCreatedAt(LocalDateTime.now());

        List<Message> messages = List.of(userMessage, assistantMessage);
        String currentMessage = "What's the weather like today?";
        String searchContext = "Weather in Beijing: Sunny, 25°C";

        // 执行测试
        String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

        // 验证结果
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("你是一个智能助手");
        assertThat(prompt).contains("搜索相关信息");
        assertThat(prompt).contains("Weather in Beijing: Sunny, 25°C");
        assertThat(prompt).contains("历史对话");
        assertThat(prompt).contains("User: Hello");
        assertThat(prompt).contains("Assistant: Hello! How can I help you?");
        assertThat(prompt).contains("User: What's the weather like today?");
        assertThat(prompt).contains("Assistant: ");
    }

    @Test
    void testBuildPromptFromMessagesWithNullSearchContext() {
        // 准备测试数据
        Message userMessage = new Message();
        userMessage.setRole("user");
        userMessage.setContent("Hello");
        userMessage.setCreatedAt(LocalDateTime.now());

        List<Message> messages = List.of(userMessage);
        String currentMessage = "What's the weather like today?";
        String searchContext = null;

        // 执行测试
        String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

        // 验证结果
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("你是一个智能助手");
        assertThat(prompt).doesNotContain("搜索相关信息：");
        assertThat(prompt).contains("历史对话");
        assertThat(prompt).contains("User: Hello");
        assertThat(prompt).contains("User: What's the weather like today?");
        assertThat(prompt).contains("Assistant: ");
    }

    @Test
    void testBuildPromptFromMessagesWithEmptyMessages() {
        // 准备测试数据
        List<Message> messages = List.of();
        String currentMessage = "What's the weather like today?";
        String searchContext = "Weather in Beijing: Sunny, 25°C";

        // 执行测试
        String prompt = promptBuilder.buildPromptFromMessages(messages, currentMessage, searchContext);

        // 验证结果
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("你是一个智能助手");
        assertThat(prompt).contains("搜索相关信息");
        assertThat(prompt).contains("Weather in Beijing: Sunny, 25°C");
        assertThat(prompt).doesNotContain("历史对话");
        assertThat(prompt).contains("User: What's the weather like today?");
        assertThat(prompt).contains("Assistant: ");
    }

    @Test
    void testFormatSearchContext() {
        // 准备测试数据
        String searchResults = "Result 1\nResult 2\n  \nResult 3";

        // 执行测试
        String formattedContext = promptBuilder.formatSearchContext(searchResults);

        // 验证结果
        assertThat(formattedContext).isNotNull();
        assertThat(formattedContext).contains("- Result 1");
        assertThat(formattedContext).contains("- Result 2");
        assertThat(formattedContext).contains("- Result 3");
    }

    @Test
    void testFormatSearchContextWithNullResults() {
        // 准备测试数据
        String searchResults = null;

        // 执行测试
        String formattedContext = promptBuilder.formatSearchContext(searchResults);

        // 验证结果
        assertThat(formattedContext).isNotNull();
        assertThat(formattedContext).isEmpty();
    }

    @Test
    void testFormatSearchContextWithEmptyResults() {
        // 准备测试数据
        String searchResults = "";

        // 执行测试
        String formattedContext = promptBuilder.formatSearchContext(searchResults);

        // 验证结果
        assertThat(formattedContext).isNotNull();
        assertThat(formattedContext).isEmpty();
    }
}