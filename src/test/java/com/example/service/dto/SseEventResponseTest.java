package com.example.service.dto;

import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

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

  @Test
  void shouldTestSseEventResponseEquality() {
    // Given
    SseEventResponse event1 = new SseEventResponse("test", "data");
    SseEventResponse event2 = new SseEventResponse("test", "data");
    SseEventResponse event3 = new SseEventResponse("test", "different");
    SseEventResponse event4 = new SseEventResponse("different", "data");

    // Then
    assertEquals(event1, event2);
    assertNotEquals(event1, event3);
    assertNotEquals(event1, event4);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void shouldTestSseEventResponseWithNullType() {
    // Given
    SseEventResponse event1 = new SseEventResponse(null, "data");
    SseEventResponse event2 = new SseEventResponse(null, "data");
    SseEventResponse event3 = new SseEventResponse("type", "data");

    // Then
    assertEquals(event1, event2);
    assertNotEquals(event1, event3);
  }

  @Test
  void shouldTestSseEventResponseWithNullData() {
    // Given
    SseEventResponse event1 = new SseEventResponse("type", null);
    SseEventResponse event2 = new SseEventResponse("type", null);
    SseEventResponse event3 = new SseEventResponse("type", "data");

    // Then
    assertEquals(event1, event2);
    assertNotEquals(event1, event3);
  }

  @Test
  void shouldTestChunkDataWithNullContent() {
    // Given
    SseEventResponse.ChunkData data1 = new SseEventResponse.ChunkData(null);
    SseEventResponse.ChunkData data2 = new SseEventResponse.ChunkData(null);
    SseEventResponse.ChunkData data3 = new SseEventResponse.ChunkData("content");

    // Then
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
  }

  @Test
  void shouldTestSearchDataWithNullType() {
    // Given
    SseEventResponse.SearchData data1 = new SseEventResponse.SearchData(null);
    SseEventResponse.SearchData data2 = new SseEventResponse.SearchData(null);
    SseEventResponse.SearchData data3 = new SseEventResponse.SearchData("type");

    // Then
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
  }

  @Test
  void shouldTestSearchResultsDataWithNullResults() {
    // Given
    SseEventResponse.SearchResultsData data1 = new SseEventResponse.SearchResultsData(null);
    SseEventResponse.SearchResultsData data2 = new SseEventResponse.SearchResultsData(null);
    SseEventResponse.SearchResultsData data3 = new SseEventResponse.SearchResultsData("results");

    // Then
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
  }

  @Test
  void shouldTestEndDataWithNullMessageId() {
    // Given
    SseEventResponse.EndData data1 = new SseEventResponse.EndData(null);
    SseEventResponse.EndData data2 = new SseEventResponse.EndData(null);
    SseEventResponse.EndData data3 = new SseEventResponse.EndData(1L);

    // Then
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
  }

  @Test
  void shouldTestToStringMethods() {
    // Given
    SseEventResponse event = new SseEventResponse("test", "data");
    SseEventResponse.ChunkData chunkData = new SseEventResponse.ChunkData("content");
    SseEventResponse.SearchData searchData = new SseEventResponse.SearchData("searching");
    SseEventResponse.SearchResultsData resultsData = new SseEventResponse.SearchResultsData("results");
    SseEventResponse.EndData endData = new SseEventResponse.EndData(123L);

    // When & Then
    assertNotNull(event.toString());
    assertTrue(event.toString().contains("test"));
    assertTrue(event.toString().contains("data"));
    
    assertNotNull(chunkData.toString());
    assertTrue(chunkData.toString().contains("content"));
    
    assertNotNull(searchData.toString());
    assertTrue(searchData.toString().contains("searching"));
    
    assertNotNull(resultsData.toString());
    assertTrue(resultsData.toString().contains("results"));
    
    assertNotNull(endData.toString());
    assertTrue(endData.toString().contains("123"));
  }

  @Test
  void shouldTestHashCodeConsistency() {
    // Given
    SseEventResponse event = new SseEventResponse("test", "data");
    
    int hashCode1 = event.hashCode();
    int hashCode2 = event.hashCode();
    
    // Then
    assertEquals(hashCode1, hashCode2);
  }

  @Test
  void shouldCreateEventWithEmptyStrings() {
    // Given & When
    SseEventResponse startEvent = SseEventResponse.start("");
    SseEventResponse chunkEvent = SseEventResponse.chunk("");
    SseEventResponse thinkingEvent = SseEventResponse.thinking("");
    SseEventResponse searchEvent = SseEventResponse.search("");
    SseEventResponse errorEvent = SseEventResponse.error("");
    
    // Then
    assertEquals("start", startEvent.getType());
    assertEquals("", startEvent.getData());
    
    assertEquals("chunk", chunkEvent.getType());
    SseEventResponse.ChunkData chunkData = (SseEventResponse.ChunkData) chunkEvent.getData();
    assertEquals("", chunkData.getContent());
    
    assertEquals("thinking", thinkingEvent.getType());
    SseEventResponse.ChunkData thinkingData = (SseEventResponse.ChunkData) thinkingEvent.getData();
    assertEquals("", thinkingData.getContent());
    
    assertEquals("search", searchEvent.getType());
    SseEventResponse.SearchData searchData = (SseEventResponse.SearchData) searchEvent.getData();
    assertEquals("", searchData.getType());
    
    assertEquals("error", errorEvent.getType());
    assertEquals("", errorEvent.getData());
  }

  @Test
  void shouldCreateEventWithNullInputs() {
    // Given & When
    SseEventResponse startEvent = SseEventResponse.start(null);
    SseEventResponse chunkEvent = SseEventResponse.chunk(null);
    SseEventResponse thinkingEvent = SseEventResponse.thinking(null);
    SseEventResponse searchEvent = SseEventResponse.search(null);
    SseEventResponse searchResultsEvent = SseEventResponse.searchResults(null);
    SseEventResponse endEvent = SseEventResponse.end(null);
    SseEventResponse errorEvent = SseEventResponse.error(null);
    
    // Then
    assertEquals("start", startEvent.getType());
    assertNull(startEvent.getData());
    
    assertEquals("chunk", chunkEvent.getType());
    SseEventResponse.ChunkData chunkData = (SseEventResponse.ChunkData) chunkEvent.getData();
    assertNull(chunkData.getContent());
    
    assertEquals("thinking", thinkingEvent.getType());
    SseEventResponse.ChunkData thinkingData = (SseEventResponse.ChunkData) thinkingEvent.getData();
    assertNull(thinkingData.getContent());
    
    assertEquals("search", searchEvent.getType());
    SseEventResponse.SearchData searchData = (SseEventResponse.SearchData) searchEvent.getData();
    assertNull(searchData.getType());
    
    assertEquals("search_results", searchResultsEvent.getType());
    SseEventResponse.SearchResultsData resultsData = (SseEventResponse.SearchResultsData) searchResultsEvent.getData();
    assertNull(resultsData.getResults());
    
    assertEquals("end", endEvent.getType());
    SseEventResponse.EndData endData = (SseEventResponse.EndData) endEvent.getData();
    assertNull(endData.getMessageId());
    
    assertEquals("error", errorEvent.getType());
    assertNull(errorEvent.getData());
  }

  @Test
  void shouldTestAllArgsConstructor() {
    // Given
    String type = "custom";
    Object data = "custom data";
    
    // When
    SseEventResponse event = new SseEventResponse(type, data);
    
    // Then
    assertEquals(type, event.getType());
    assertEquals(data, event.getData());
  }

  @Test
  void shouldTestNoArgsConstructor() {
    // When
    SseEventResponse event = new SseEventResponse();
    
    // Then
    assertNull(event.getType());
    assertNull(event.getData());
  }

  @Test
  void shouldTestSettersAndGetters() {
    // Given
    SseEventResponse event = new SseEventResponse();
    
    // When
    event.setType("test");
    event.setData("test data");
    
    // Then
    assertEquals("test", event.getType());
    assertEquals("test data", event.getData());
  }

  @Test
  void shouldTestSseEventResponseEqualsWithSameInstance() {
    SseEventResponse event = new SseEventResponse();
    event.setType("test");
    assertEquals(event, event); // Same instance reference
  }

  @Test
  void shouldTestSseEventResponseEqualsWithNull() {
    SseEventResponse event = new SseEventResponse();
    assertNotEquals(event, null); // Null comparison
  }

  @Test
  void shouldTestSseEventResponseEqualsWithDifferentType() {
    SseEventResponse event = new SseEventResponse();
    assertNotEquals(event, "not an SseEventResponse"); // Different type
  }

  @Test
  void shouldTestSseEventResponseEqualsWithCanEqualFalse() {
    SseEventResponse event1 = new SseEventResponse();
    event1.setType("test");
    
    // Anonymous subclass that overrides canEqual to return false
    SseEventResponse event2 = new SseEventResponse() {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    event2.setType("test");
    
    assertNotEquals(event1, event2); // canEqual returns false
  }

  @Test
  void shouldTestChunkDataEqualsWithSameInstance() {
    SseEventResponse.ChunkData data = new SseEventResponse.ChunkData("content");
    assertEquals(data, data); // Same instance reference
  }

  @Test
  void shouldTestChunkDataEqualsWithNull() {
    SseEventResponse.ChunkData data = new SseEventResponse.ChunkData("content");
    assertNotEquals(data, null); // Null comparison
  }

  @Test
  void shouldTestChunkDataEqualsWithDifferentType() {
    SseEventResponse.ChunkData data = new SseEventResponse.ChunkData("content");
    assertNotEquals(data, "not a ChunkData"); // Different type
  }

  @Test
  void shouldTestChunkDataEqualsWithCanEqualFalse() {
    SseEventResponse.ChunkData data1 = new SseEventResponse.ChunkData("content");
    
    // Anonymous subclass that overrides canEqual to return false
    SseEventResponse.ChunkData data2 = new SseEventResponse.ChunkData("content") {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    
    assertNotEquals(data1, data2); // canEqual returns false
  }

  @Test
  void shouldTestEndDataEqualsWithSameInstance() {
    SseEventResponse.EndData data = new SseEventResponse.EndData(1L);
    assertEquals(data, data); // Same instance reference
  }

  @Test
  void shouldTestEndDataEqualsWithNull() {
    SseEventResponse.EndData data = new SseEventResponse.EndData(1L);
    assertNotEquals(data, null); // Null comparison
  }

  @Test
  void shouldTestEndDataEqualsWithDifferentType() {
    SseEventResponse.EndData data = new SseEventResponse.EndData(1L);
    assertNotEquals(data, "not an EndData"); // Different type
  }

  @Test
  void shouldTestEndDataEqualsWithCanEqualFalse() {
    SseEventResponse.EndData data1 = new SseEventResponse.EndData(1L);
    
    // Anonymous subclass that overrides canEqual to return false
    SseEventResponse.EndData data2 = new SseEventResponse.EndData(1L) {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    
    assertNotEquals(data1, data2); // canEqual returns false
  }

  @Test
  void shouldTestSearchDataEqualsWithSameInstance() {
    SseEventResponse.SearchData data = new SseEventResponse.SearchData("searching");
    assertEquals(data, data); // Same instance reference
  }

  @Test
  void shouldTestSearchDataEqualsWithNull() {
    SseEventResponse.SearchData data = new SseEventResponse.SearchData("searching");
    assertNotEquals(data, null); // Null comparison
  }

  @Test
  void shouldTestSearchDataEqualsWithDifferentType() {
    SseEventResponse.SearchData data = new SseEventResponse.SearchData("searching");
    assertNotEquals(data, "not a SearchData"); // Different type
  }

  @Test
  void shouldTestSearchDataEqualsWithCanEqualFalse() {
    SseEventResponse.SearchData data1 = new SseEventResponse.SearchData("searching");
    
    // Anonymous subclass that overrides canEqual to return false
    SseEventResponse.SearchData data2 = new SseEventResponse.SearchData("searching") {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    
    assertNotEquals(data1, data2); // canEqual returns false
  }

  @Test
  void shouldTestSearchResultsDataEqualsWithSameInstance() {
    SseEventResponse.SearchResultsData data = new SseEventResponse.SearchResultsData("results");
    assertEquals(data, data); // Same instance reference
  }

  @Test
  void shouldTestSearchResultsDataEqualsWithNull() {
    SseEventResponse.SearchResultsData data = new SseEventResponse.SearchResultsData("results");
    assertNotEquals(data, null); // Null comparison
  }

  @Test
  void shouldTestSearchResultsDataEqualsWithDifferentType() {
    SseEventResponse.SearchResultsData data = new SseEventResponse.SearchResultsData("results");
    assertNotEquals(data, "not a SearchResultsData"); // Different type
  }

  @Test
  void shouldTestSearchResultsDataEqualsWithCanEqualFalse() {
    SseEventResponse.SearchResultsData data1 = new SseEventResponse.SearchResultsData("results");
    
    // Anonymous subclass that overrides canEqual to return false
    SseEventResponse.SearchResultsData data2 = new SseEventResponse.SearchResultsData("results") {
      @Override
      public boolean canEqual(Object other) {
        return false;
      }
    };
    
    assertNotEquals(data1, data2); // canEqual returns false
  }

  @Test
  void shouldTestSseEventResponseEqualsWithOneNullTypeField() {
    SseEventResponse event1 = new SseEventResponse();
    event1.setType("test");
    event1.setData("data");
    
    SseEventResponse event2 = new SseEventResponse();
    event2.setType(null);
    event2.setData("data");
    
    assertNotEquals(event1, event2); // One has null type, other doesn't
  }

  @Test
  void shouldTestSseEventResponseEqualsWithOneNullDataField() {
    SseEventResponse event1 = new SseEventResponse();
    event1.setType("test");
    event1.setData("data");
    
    SseEventResponse event2 = new SseEventResponse();
    event2.setType("test");
    event2.setData(null);
    
    assertNotEquals(event1, event2); // One has null data, other doesn't
  }

  @Test
  void shouldTestChunkDataEqualsWithOneNullContentField() {
    SseEventResponse.ChunkData data1 = new SseEventResponse.ChunkData("content");
    SseEventResponse.ChunkData data2 = new SseEventResponse.ChunkData(null);
    
    assertNotEquals(data1, data2); // One has null content, other doesn't
  }

  @Test
  void shouldTestEndDataEqualsWithOneNullMessageIdField() {
    SseEventResponse.EndData data1 = new SseEventResponse.EndData(1L);
    SseEventResponse.EndData data2 = new SseEventResponse.EndData(null);
    
    assertNotEquals(data1, data2); // One has null messageId, other doesn't
  }

  @Test
  void shouldTestSearchDataEqualsWithOneNullTypeField() {
    SseEventResponse.SearchData data1 = new SseEventResponse.SearchData("searching");
    SseEventResponse.SearchData data2 = new SseEventResponse.SearchData(null);
    
    assertNotEquals(data1, data2); // One has null type, other doesn't
  }

  @Test
  void shouldTestSearchResultsDataEqualsWithOneNullResultsField() {
    SseEventResponse.SearchResultsData data1 = new SseEventResponse.SearchResultsData("results");
    SseEventResponse.SearchResultsData data2 = new SseEventResponse.SearchResultsData(null);
    
    assertNotEquals(data1, data2); // One has null results, other doesn't
  }

  @Test
  void shouldTestHashCodeWithAllNullFields() {
    SseEventResponse event1 = new SseEventResponse();
    SseEventResponse event2 = new SseEventResponse();
    
    SseEventResponse.ChunkData chunkData1 = new SseEventResponse.ChunkData(null);
    SseEventResponse.ChunkData chunkData2 = new SseEventResponse.ChunkData(null);
    
    SseEventResponse.EndData endData1 = new SseEventResponse.EndData(null);
    SseEventResponse.EndData endData2 = new SseEventResponse.EndData(null);
    
    SseEventResponse.SearchData searchData1 = new SseEventResponse.SearchData(null);
    SseEventResponse.SearchData searchData2 = new SseEventResponse.SearchData(null);
    
    SseEventResponse.SearchResultsData resultsData1 = new SseEventResponse.SearchResultsData(null);
    SseEventResponse.SearchResultsData resultsData2 = new SseEventResponse.SearchResultsData(null);
    
    assertEquals(event1.hashCode(), event2.hashCode());
    assertEquals(chunkData1.hashCode(), chunkData2.hashCode());
    assertEquals(endData1.hashCode(), endData2.hashCode());
    assertEquals(searchData1.hashCode(), searchData2.hashCode());
    assertEquals(resultsData1.hashCode(), resultsData2.hashCode());
  }
}