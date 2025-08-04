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
        
        ApiResponse<String> response3 = new ApiResponse<>();
        response3.setSuccess(false);
        response3.setMessage("test");
        response3.setData("data");
        
        // Then
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, response3);
        
        // Test equals with null
        assertNotEquals(response1, null);
        
        // Test equals with different class
        assertNotEquals(response1, "not a response");
        
        // Test equals with same object
        assertEquals(response1, response1);
        
        // Test canEqual method
        assertTrue(response1.canEqual(response2));
        assertFalse(response1.canEqual("not a response"));
        assertFalse(response1.canEqual(null));
        
        // Test with different success values
        ApiResponse<String> responseDiffSuccess = new ApiResponse<>();
        responseDiffSuccess.setSuccess(false);
        responseDiffSuccess.setMessage("test");
        responseDiffSuccess.setData("data");
        
        assertNotEquals(response1, responseDiffSuccess);
        
        // Test with different message
        ApiResponse<String> responseDiffMessage = new ApiResponse<>();
        responseDiffMessage.setSuccess(true);
        responseDiffMessage.setMessage("different");
        responseDiffMessage.setData("data");
        
        assertNotEquals(response1, responseDiffMessage);
        
        // Test with different data
        ApiResponse<String> responseDiffData = new ApiResponse<>();
        responseDiffData.setSuccess(true);
        responseDiffData.setMessage("test");
        responseDiffData.setData("different");
        
        assertNotEquals(response1, responseDiffData);
        
        // Test with null message
        ApiResponse<String> responseNullMessage1 = new ApiResponse<>();
        responseNullMessage1.setSuccess(true);
        responseNullMessage1.setMessage(null);
        responseNullMessage1.setData("data");
        
        ApiResponse<String> responseNullMessage2 = new ApiResponse<>();
        responseNullMessage2.setSuccess(true);
        responseNullMessage2.setMessage(null);
        responseNullMessage2.setData("data");
        
        assertEquals(responseNullMessage1, responseNullMessage2);
        assertEquals(responseNullMessage1.hashCode(), responseNullMessage2.hashCode());
        
        // Test with null data
        ApiResponse<String> responseNullData1 = new ApiResponse<>();
        responseNullData1.setSuccess(true);
        responseNullData1.setMessage("test");
        responseNullData1.setData(null);
        
        ApiResponse<String> responseNullData2 = new ApiResponse<>();
        responseNullData2.setSuccess(true);
        responseNullData2.setMessage("test");
        responseNullData2.setData(null);
        
        assertEquals(responseNullData1, responseNullData2);
        assertEquals(responseNullData1.hashCode(), responseNullData2.hashCode());
        
        // Test null vs non-null message
        ApiResponse<String> responseMessageNull = new ApiResponse<>();
        responseMessageNull.setMessage(null);
        
        ApiResponse<String> responseMessageNotNull = new ApiResponse<>();
        responseMessageNotNull.setMessage("message");
        
        assertNotEquals(responseMessageNull, responseMessageNotNull);
        assertNotEquals(responseMessageNotNull, responseMessageNull);
        
        // Test null vs non-null data
        ApiResponse<String> responseDataNull = new ApiResponse<>();
        responseDataNull.setData(null);
        
        ApiResponse<String> responseDataNotNull = new ApiResponse<>();
        responseDataNotNull.setData("data");
        
        assertNotEquals(responseDataNull, responseDataNotNull);
        assertNotEquals(responseDataNotNull, responseDataNull);
        
        // Test hashCode consistency
        int hashCode1 = response1.hashCode();
        int hashCode2 = response1.hashCode();
        assertEquals(hashCode1, hashCode2);
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