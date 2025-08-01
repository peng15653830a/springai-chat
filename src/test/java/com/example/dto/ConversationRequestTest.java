package com.example.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConversationRequestTest {

    @Test
    void testDefaultConstructor() {
        // When
        ConversationRequest request = new ConversationRequest();
        
        // Then
        assertNull(request.getTitle());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        ConversationRequest request = new ConversationRequest();
        String title = "测试对话标题";
        
        // When
        request.setTitle(title);
        
        // Then
        assertEquals(title, request.getTitle());
    }
    
    @Test
    void testWithNullTitle() {
        // Given
        ConversationRequest request = new ConversationRequest();
        
        // When
        request.setTitle(null);
        
        // Then
        assertNull(request.getTitle());
    }
    
    @Test
    void testWithEmptyTitle() {
        // Given
        ConversationRequest request = new ConversationRequest();
        String emptyTitle = "";
        
        // When
        request.setTitle(emptyTitle);
        
        // Then
        assertEquals(emptyTitle, request.getTitle());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        ConversationRequest request1 = new ConversationRequest();
        request1.setTitle("相同标题");
        
        ConversationRequest request2 = new ConversationRequest();
        request2.setTitle("相同标题");
        
        // Then
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testToString() {
        // Given
        ConversationRequest request = new ConversationRequest();
        request.setTitle("测试对话");
        
        // When
        String toString = request.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("title=测试对话"));
    }
    
    @Test
    void testLongTitle() {
        // Given
        ConversationRequest request = new ConversationRequest();
        String longTitle = "这是一个非常长的对话标题，用来测试系统是否能够正确处理长标题的情况";
        
        // When
        request.setTitle(longTitle);
        
        // Then
        assertEquals(longTitle, request.getTitle());
    }
    
    @Test
    void testSpecialCharactersInTitle() {
        // Given
        ConversationRequest request = new ConversationRequest();
        String specialTitle = "特殊字符@#$%^&*()测试";
        
        // When
        request.setTitle(specialTitle);
        
        // Then
        assertEquals(specialTitle, request.getTitle());
    }
    
    @Test
    void testWhitespaceTitle() {
        // Given
        ConversationRequest request = new ConversationRequest();
        String whitespaceTitle = "   ";
        
        // When
        request.setTitle(whitespaceTitle);
        
        // Then
        assertEquals(whitespaceTitle, request.getTitle());
    }
}