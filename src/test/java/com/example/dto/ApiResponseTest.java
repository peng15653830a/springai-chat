package com.example.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessWithData() {
        // Given
        String testData = "test data";
        
        // When
        ApiResponse<String> response = ApiResponse.success(testData);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertNull(response.getMessage());
    }
    
    @Test
    void testSuccessWithMessageAndData() {
        // Given
        String message = "操作成功";
        String testData = "test data";
        
        // When
        ApiResponse<String> response = ApiResponse.success(message, testData);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(testData, response.getData());
    }
    
    @Test
    void testErrorWithMessage() {
        // Given
        String errorMessage = "操作失败";
        
        // When
        ApiResponse<Object> response = ApiResponse.error(errorMessage);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }
    
    @Test
    void testErrorWithCodeAndMessage() {
        // Given
        String code = "ERROR_001";
        String message = "系统错误";
        
        // When
        ApiResponse<Object> response = ApiResponse.error(code, message);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
    }
    
    @Test
    void testErrorWithCodeMessageAndData() {
        // Given
        String code = "ERROR_002";
        String message = "验证失败";
        String errorData = "error details";
        
        // When
        ApiResponse<String> response = ApiResponse.error(code, message, errorData);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(errorData, response.getData());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        ApiResponse<String> response = new ApiResponse<>();
        
        // When
        response.setSuccess(true);
        response.setMessage("测试消息");
        response.setData("测试数据");
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals("测试消息", response.getMessage());
        assertEquals("测试数据", response.getData());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        ApiResponse<String> response1 = new ApiResponse<>();
        response1.setSuccess(true);
        response1.setMessage("test");
        response1.setData("data");
        
        ApiResponse<String> response2 = new ApiResponse<>();
        response2.setSuccess(true);
        response2.setMessage("test");
        response2.setData("data");
        
        // Then
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    void testToString() {
        // Given
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("test message");
        response.setData("test data");
        
        // When
        String toString = response.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("message=test message"));
        assertTrue(toString.contains("data=test data"));
    }
    
    @Test
    void testGenericTypes() {
        // Given & When
        ApiResponse<Integer> intResponse = ApiResponse.success(123);
        ApiResponse<Boolean> boolResponse = ApiResponse.success(true);
        
        // Then
        assertEquals(Integer.valueOf(123), intResponse.getData());
        assertEquals(Boolean.TRUE, boolResponse.getData());
    }
}