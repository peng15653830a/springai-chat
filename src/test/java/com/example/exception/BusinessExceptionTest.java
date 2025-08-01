package com.example.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void testBusinessExceptionWithMessage() {
        // Given
        String message = "业务异常测试";
        
        // When
        BusinessException exception = new BusinessException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("BUSINESS_ERROR", exception.getCode());
        assertNull(exception.getCause());
    }
    
    @Test
    void testBusinessExceptionWithCodeAndMessage() {
        // Given
        String code = "CUSTOM_ERROR";
        String message = "自定义业务异常";
        
        // When
        BusinessException exception = new BusinessException(code, message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
        assertNull(exception.getCause());
    }
    
    @Test
    void testBusinessExceptionWithMessageAndCause() {
        // Given
        String message = "业务异常测试";
        Throwable cause = new RuntimeException("根本原因");
        
        // When
        BusinessException exception = new BusinessException(message, cause);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("BUSINESS_ERROR", exception.getCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testBusinessExceptionWithCodeMessageAndCause() {
        // Given
        String code = "VALIDATION_ERROR";
        String message = "验证失败";
        Throwable cause = new IllegalArgumentException("参数错误");
        
        // When
        BusinessException exception = new BusinessException(code, message, cause);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testGetAndSetCode() {
        // Given
        BusinessException exception = new BusinessException("测试消息");
        String newCode = "NEW_ERROR_CODE";
        
        // When
        exception.setCode(newCode);
        
        // Then
        assertEquals(newCode, exception.getCode());
    }
    
    @Test
    void testBusinessExceptionInheritance() {
        // Given
        BusinessException exception = new BusinessException("测试");
        
        // Then
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }
    
    @Test
    void testDefaultCode() {
        // Given & When
        BusinessException exception1 = new BusinessException("消息1");
        BusinessException exception2 = new BusinessException("消息2", new RuntimeException());
        
        // Then
        assertEquals("BUSINESS_ERROR", exception1.getCode());
        assertEquals("BUSINESS_ERROR", exception2.getCode());
    }
    
    @Test
    void testCustomCode() {
        // Given & When
        BusinessException exception1 = new BusinessException("CUSTOM1", "消息1");
        BusinessException exception2 = new BusinessException("CUSTOM2", "消息2", new RuntimeException());
        
        // Then
        assertEquals("CUSTOM1", exception1.getCode());
        assertEquals("CUSTOM2", exception2.getCode());
    }
}