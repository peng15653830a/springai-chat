package com.example.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiResponseTest {

    @Test
    void testSuccessResponse() {
        // Given
        String testData = "Test data";

        // When
        ApiResponse<String> response = ApiResponse.success(testData);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertNull(response.getMessage());
    }

    @Test
    void testSuccessResponseWithMessage() {
        // Given
        String testData = "Test data";
        String message = "Operation successful";

        // When
        ApiResponse<String> response = ApiResponse.success(message, testData);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertEquals(message, response.getMessage());
    }

    @Test
    void testErrorResponse() {
        // Given
        String errorMessage = "Something went wrong";

        // When
        ApiResponse<Void> response = ApiResponse.error(errorMessage);

        // Then
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(errorMessage, response.getMessage());
    }

    @Test
    void testErrorResponseWithData() {
        // Given
        String errorCode = "VALIDATION_ERROR";
        String errorMessage = "Validation failed";
        String errorData = "Field error details";

        // When
        ApiResponse<String> response = ApiResponse.error(errorCode, errorMessage, errorData);

        // Then
        assertFalse(response.isSuccess());
        assertEquals(errorData, response.getData());
        assertEquals(errorMessage, response.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        ApiResponse<String> response = new ApiResponse<>();
        String data = "Test data";
        String message = "Test message";

        // When
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals(message, response.getMessage());
    }

    @Test
    void testNullValues() {
        // When
        ApiResponse<String> response = ApiResponse.success(null);

        // Then
        assertTrue(response.isSuccess());
        assertNull(response.getData());

        // When
        ApiResponse<Void> errorResponse = ApiResponse.error(null);

        // Then
        assertFalse(errorResponse.isSuccess());
        assertNull(errorResponse.getMessage());
    }

    @Test
    void testEmptyMessage() {
        // When
        ApiResponse<Void> response = ApiResponse.error("");

        // Then
        assertFalse(response.isSuccess());
        assertEquals("", response.getMessage());
    }

    @Test
    void testGenericTypes() {
        // When
        ApiResponse<Integer> intResponse = ApiResponse.success(42);
        ApiResponse<Boolean> boolResponse = ApiResponse.success(true);

        // Then
        assertTrue(intResponse.isSuccess());
        assertEquals(Integer.valueOf(42), intResponse.getData());

        assertTrue(boolResponse.isSuccess());
        assertEquals(Boolean.TRUE, boolResponse.getData());
    }
}