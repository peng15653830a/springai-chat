package com.example.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageRequestTest {

    @Test
    void testDefaultConstructor() {
        // When
        MessageRequest request = new MessageRequest();
        
        // Then
        assertNull(request.getContent());
        assertTrue(request.getSearchEnabled()); // 默认开启搜索
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        MessageRequest request = new MessageRequest();
        String content = "测试消息内容";
        Boolean searchEnabled = false;
        
        // When
        request.setContent(content);
        request.setSearchEnabled(searchEnabled);
        
        // Then
        assertEquals(content, request.getContent());
        assertEquals(searchEnabled, request.getSearchEnabled());
    }
    
    @Test
    void testSearchEnabledDefault() {
        // Given & When
        MessageRequest request = new MessageRequest();
        
        // Then
        assertTrue(request.getSearchEnabled());
    }
    
    @Test
    void testSetSearchEnabledToNull() {
        // Given
        MessageRequest request = new MessageRequest();
        
        // When
        request.setSearchEnabled(null);
        
        // Then
        assertNull(request.getSearchEnabled());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        MessageRequest request1 = new MessageRequest();
        request1.setContent("test content");
        request1.setSearchEnabled(true);
        
        MessageRequest request2 = new MessageRequest();
        request2.setContent("test content");
        request2.setSearchEnabled(true);
        
        // Then
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testToString() {
        // Given
        MessageRequest request = new MessageRequest();
        request.setContent("test message");
        request.setSearchEnabled(false);
        
        // When
        String toString = request.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("content=test message"));
        assertTrue(toString.contains("searchEnabled=false"));
    }
    
    @Test
    void testWithNullContent() {
        // Given
        MessageRequest request = new MessageRequest();
        
        // When
        request.setContent(null);
        
        // Then
        assertNull(request.getContent());
        assertTrue(request.getSearchEnabled()); // 默认值不变
    }
    
    @Test
    void testWithEmptyContent() {
        // Given
        MessageRequest request = new MessageRequest();
        String emptyContent = "";
        
        // When
        request.setContent(emptyContent);
        
        // Then
        assertEquals(emptyContent, request.getContent());
    }
}