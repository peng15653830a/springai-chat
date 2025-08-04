package com.example.exception;

import com.example.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/test/path");
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("参数无效");

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleIllegalArgumentException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("参数无效", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testHandleBusinessException() {
        // Given
        BusinessException exception = new BusinessException("业务异常");

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("业务异常", response.getMessage());
    }

    @Test
    void testHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("运行时异常");

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleRuntimeException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("系统运行异常: 运行时异常", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testHandleGeneralException() {
        // Given
        Exception exception = new Exception("通用异常");

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleGeneralException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("系统错误，请联系管理员", response.getMessage());
        assertNull(response.getData());
    }


    @Test
    void testHandleNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("空指针异常");

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleNullPointerException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("系统内部错误，请联系管理员", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testHandleTypeMismatchException() {
        // Given
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception =
                mock(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getRequiredType()).thenReturn((Class) Long.class);

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleTypeMismatchException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("参数"));
        assertNull(response.getData());
    }


    @Test
    void testHandleIllegalArgumentExceptionWithNullMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException();

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleIllegalArgumentException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    void testHandleRuntimeExceptionWithNullMessage() {
        // Given
        RuntimeException exception = new RuntimeException();

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleRuntimeException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().startsWith("系统运行异常:"));
        assertNull(response.getData());
    }

    @Test
    void testRequestUriLogging() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/test");
        IllegalArgumentException exception = new IllegalArgumentException("测试异常");

        // When
        globalExceptionHandler.handleIllegalArgumentException(exception, request);

        // Then
        verify(request, atLeastOnce()).getRequestURI();
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("testObject", "field1", "Field1 error");
        FieldError fieldError2 = new FieldError("testObject", "field2", "Field2 error");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleValidationException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("参数验证失败", response.getMessage());
        assertNotNull(response.getData());
        
        // Verify the data contains error details
        assertTrue(response.getData() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorMap = (Map<String, String>) response.getData();
        assertEquals("Field1 error", errorMap.get("field1"));
        assertEquals("Field2 error", errorMap.get("field2"));
    }

    @Test
    void testHandleBindException() {
        // Given
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("testObject", "testField", "Test error");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError));

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleBindException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("参数绑定失败", response.getMessage());
        assertNotNull(response.getData());
        
        // Verify the data contains error details
        assertTrue(response.getData() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorMap = (Map<String, String>) response.getData();
        assertEquals("Test error", errorMap.get("testField"));
    }

    @Test
    void testHandleTypeMismatchException_WithNullRequiredType() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("testParam");
        when(exception.getRequiredType()).thenReturn(null);

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleTypeMismatchException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("testParam"));
        assertTrue(response.getMessage().contains("unknown"));
    }

    @Test
    void testHandleBusinessException_WithNullCode() {
        // Given
        BusinessException exception = new BusinessException(null, "业务异常消息");

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("业务异常消息", response.getMessage());
    }

    @Test
    void testHandleValidationException_EmptyErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList());

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleValidationException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("参数验证失败", response.getMessage());
        assertTrue(response.getData() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorMap = (Map<String, String>) response.getData();
        assertTrue(errorMap.isEmpty());
    }

    @Test
    void testHandleBindException_EmptyErrors() {
        // Given
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList());

        // When
        ApiResponse<Object> response = globalExceptionHandler.handleBindException(exception, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("参数绑定失败", response.getMessage());
        assertTrue(response.getData() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorMap = (Map<String, String>) response.getData();
        assertTrue(errorMap.isEmpty());
    }
}