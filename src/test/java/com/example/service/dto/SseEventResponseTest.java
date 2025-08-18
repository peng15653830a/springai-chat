package com.example.service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SseEventResponse测试
 *
 * @author xupeng
 */
class SseEventResponseTest {

  @Test
  void shouldCreateStartEvent() {
    // Given
    String message = "AI正在思考中...";

    // When
    SseEventResponse event = SseEventResponse.start(message);

    // Then
    assertEquals("start", event.getType());
    assertEquals(message, event.getData());
  }

  @Test
  void shouldCreateChunkEvent() {
    // Given
    String content = "这是AI的回答";

    // When
    SseEventResponse event = SseEventResponse.chunk(content);

    // Then
    assertEquals("chunk", event.getType());
    assertNotNull(event.getData());
    assertTrue(event.getData() instanceof SseEventResponse.ChunkData);
    
    SseEventResponse.ChunkData chunkData = (SseEventResponse.ChunkData) event.getData();
    assertEquals(content, chunkData.getContent());
  }

  @Test
  void shouldCreateThinkingEvent() {
    // Given
    String thinking = "我需要分析这个问题...";

    // When
    SseEventResponse event = SseEventResponse.thinking(thinking);

    // Then
    assertEquals("thinking", event.getType());
    assertNotNull(event.getData());
    assertTrue(event.getData() instanceof SseEventResponse.ChunkData);
    
    SseEventResponse.ChunkData thinkingData = (SseEventResponse.ChunkData) event.getData();
    assertEquals(thinking, thinkingData.getContent());
  }

  @Test
  void shouldCreateSearchEvent() {
    // Given
    String status = "正在搜索相关信息...";

    // When
    SseEventResponse event = SseEventResponse.search(status);

    // Then
    assertEquals("search", event.getType());
    assertNotNull(event.getData());
    assertTrue(event.getData() instanceof SseEventResponse.SearchData);
    
    SseEventResponse.SearchData searchData = (SseEventResponse.SearchData) event.getData();
    assertEquals(status, searchData.getType());
  }

  @Test
  void shouldCreateSearchResultsEvent() {
    // Given
    String results = "找到相关结果";

    // When
    SseEventResponse event = SseEventResponse.searchResults(results);

    // Then
    assertEquals("search_results", event.getType());
    assertNotNull(event.getData());
    assertTrue(event.getData() instanceof SseEventResponse.SearchResultsData);
    
    SseEventResponse.SearchResultsData resultsData = (SseEventResponse.SearchResultsData) event.getData();
    assertEquals(results, resultsData.getResults());
  }

  @Test
  void shouldCreateEndEvent() {
    // Given
    Long messageId = 123L;

    // When
    SseEventResponse event = SseEventResponse.end(messageId);

    // Then
    assertEquals("end", event.getType());
    assertNotNull(event.getData());
    assertTrue(event.getData() instanceof SseEventResponse.EndData);
    
    SseEventResponse.EndData endData = (SseEventResponse.EndData) event.getData();
    assertEquals(messageId, endData.getMessageId());
  }

  @Test
  void shouldCreateErrorEvent() {
    // Given
    String errorMessage = "AI服务暂时不可用";

    // When
    SseEventResponse event = SseEventResponse.error(errorMessage);

    // Then
    assertEquals("error", event.getType());
    assertEquals(errorMessage, event.getData());
  }

  @Test
  void shouldCreateGenericEvent() {
    // Given
    String type = "custom";
    String data = "custom data";

    // When
    SseEventResponse event = SseEventResponse.of(type, data);

    // Then
    assertEquals(type, event.getType());
    assertEquals(data, event.getData());
  }

  @Test
  void shouldHandleNullData() {
    // When
    SseEventResponse event = SseEventResponse.of("test", null);

    // Then
    assertEquals("test", event.getType());
    assertNull(event.getData());
  }

  @Test
  void shouldTestChunkDataEquality() {
    // Given
    SseEventResponse.ChunkData data1 = new SseEventResponse.ChunkData("content");
    SseEventResponse.ChunkData data2 = new SseEventResponse.ChunkData("content");
    SseEventResponse.ChunkData data3 = new SseEventResponse.ChunkData("different");

    // Then
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void shouldTestEndDataEquality() {
    // Given
    SseEventResponse.EndData data1 = new SseEventResponse.EndData(1L);
    SseEventResponse.EndData data2 = new SseEventResponse.EndData(1L);
    SseEventResponse.EndData data3 = new SseEventResponse.EndData(2L);

    // Then
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void shouldTestSearchDataEquality() {
    // Given
    SseEventResponse.SearchData data1 = new SseEventResponse.SearchData("searching");
    SseEventResponse.SearchData data2 = new SseEventResponse.SearchData("searching");
    SseEventResponse.SearchData data3 = new SseEventResponse.SearchData("complete");

    // Then
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void shouldTestSearchResultsDataEquality() {
    // Given
    String results = "search results";
    SseEventResponse.SearchResultsData data1 = new SseEventResponse.SearchResultsData(results);
    SseEventResponse.SearchResultsData data2 = new SseEventResponse.SearchResultsData(results);
    SseEventResponse.SearchResultsData data3 = new SseEventResponse.SearchResultsData("different");

    // Then
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
    assertEquals(data1.hashCode(), data2.hashCode());
  }
}