package com.example.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void testDefaultConstructor() {
        // When
        LoginRequest request = new LoginRequest();
        
        // Then
        assertNull(request.getUsername());
        assertNull(request.getNickname());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        LoginRequest request = new LoginRequest();
        String username = "testuser";
        String nickname = "测试用户";
        
        // When
        request.setUsername(username);
        request.setNickname(nickname);
        
        // Then
        assertEquals(username, request.getUsername());
        assertEquals(nickname, request.getNickname());
    }
    
    @Test
    void testWithNullValues() {
        // Given
        LoginRequest request = new LoginRequest();
        
        // When
        request.setUsername(null);
        request.setNickname(null);
        
        // Then
        assertNull(request.getUsername());
        assertNull(request.getNickname());
    }
    
    @Test
    void testWithEmptyValues() {
        // Given
        LoginRequest request = new LoginRequest();
        String emptyUsername = "";
        String emptyNickname = "";
        
        // When
        request.setUsername(emptyUsername);
        request.setNickname(emptyNickname);
        
        // Then
        assertEquals(emptyUsername, request.getUsername());
        assertEquals(emptyNickname, request.getNickname());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        LoginRequest request1 = new LoginRequest();
        request1.setUsername("user1");
        request1.setNickname("用户1");
        
        LoginRequest request2 = new LoginRequest();
        request2.setUsername("user1");
        request2.setNickname("用户1");
        
        // Then
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testToString() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setNickname("测试用户");
        
        // When
        String toString = request.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("username=testuser"));
        assertTrue(toString.contains("nickname=测试用户"));
    }
    
    @Test
    void testPartialData() {
        // Given
        LoginRequest request = new LoginRequest();
        
        // When - 只设置用户名
        request.setUsername("onlyuser");
        
        // Then
        assertEquals("onlyuser", request.getUsername());
        assertNull(request.getNickname());
    }
    
    @Test
    void testSpecialCharacters() {
        // Given
        LoginRequest request = new LoginRequest();
        String specialUsername = "user@123";
        String specialNickname = "用户#123";
        
        // When
        request.setUsername(specialUsername);
        request.setNickname(specialNickname);
        
        // Then
        assertEquals(specialUsername, request.getUsername());
        assertEquals(specialNickname, request.getNickname());
    }
}