package com.example.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AiResponseTest {

  @Test
  void testConstructorWithBothParameters() {
    // Given
    String content = "AI response content";
    String thinking = "AI thinking process";

    // When
    AiResponse response = new AiResponse(content, thinking);

    // Then
    assertEquals(content, response.getContent());
    assertEquals(thinking, response.getThinking());
  }

  @Test
  void testConstructorWithContentOnly() {
    // Given
    String content = "AI response content";

    // When
    AiResponse response = new AiResponse(content, null);

    // Then
    assertEquals(content, response.getContent());
    assertNull(response.getThinking());
  }

  @Test
  void testConstructorWithThinkingOnly() {
    // Given
    String thinking = "AI thinking process";

    // When
    AiResponse response = new AiResponse(null, thinking);

    // Then
    assertNull(response.getContent());
    assertEquals(thinking, response.getThinking());
  }

  @Test
  void testConstructorWithNullValues() {
    // When
    AiResponse response = new AiResponse(null, null);

    // Then
    assertNull(response.getContent());
    assertNull(response.getThinking());
  }

  @Test
  void testGetters() {
    // Given
    String content = "Test content";
    String thinking = "Test thinking";
    AiResponse response = new AiResponse(content, thinking);

    // Then
    assertEquals(content, response.getContent());
    assertEquals(thinking, response.getThinking());
  }

  @Test
  void testEmptyStrings() {
    // When
    AiResponse response = new AiResponse("", "");

    // Then
    assertEquals("", response.getContent());
    assertEquals("", response.getThinking());
  }

  @Test
  void testLongContent() {
    // Given
    StringBuilder longContentBuilder = new StringBuilder();
    StringBuilder longThinkingBuilder = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      longContentBuilder.append(
          "This is a very long AI response content that might contain multiple paragraphs and detailed explanations. ");
    }
    for (int i = 0; i < 5; i++) {
      longThinkingBuilder.append(
          "This is a very long thinking process that shows how the AI analyzed the question and came up with the response. ");
    }
    String longContent = longContentBuilder.toString();
    String longThinking = longThinkingBuilder.toString();

    // When
    AiResponse response = new AiResponse(longContent, longThinking);

    // Then
    assertEquals(longContent, response.getContent());
    assertEquals(longThinking, response.getThinking());
  }

  @Test
  void testSpecialCharacters() {
    // Given
    String contentWithSpecialChars = "Response with special chars: @#$%^&*()! and emojis: 😀🎉🚀";
    String thinkingWithSpecialChars = "Thinking with special chars: <>{}[]|\\";

    // When
    AiResponse response = new AiResponse(contentWithSpecialChars, thinkingWithSpecialChars);

    // Then
    assertEquals(contentWithSpecialChars, response.getContent());
    assertEquals(thinkingWithSpecialChars, response.getThinking());
  }

  @Test
  void testMarkdownContent() {
    // Given
    String markdownContent =
        "## Title\n\n- List item 1\n- List item 2\n\n**Bold text** and *italic text*";
    String markdownThinking = "### Analysis\n\n1. First point\n2. Second point\n\n```code block```";

    // When
    AiResponse response = new AiResponse(markdownContent, markdownThinking);

    // Then
    assertEquals(markdownContent, response.getContent());
    assertEquals(markdownThinking, response.getThinking());
    assertTrue(response.getContent().contains("\n"));
    assertTrue(response.getThinking().contains("\n"));
  }

  @Test
  void testToString() {
    // Given
    AiResponse response = new AiResponse("Test content", "Test thinking");

    // When
    String toString = response.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("Test content") || toString.contains("AiResponse"));
  }
}
