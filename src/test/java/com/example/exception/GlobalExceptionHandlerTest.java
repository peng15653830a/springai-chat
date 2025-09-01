package com.example.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.dto.response.ApiResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @InjectMocks private GlobalExceptionHandler globalExceptionHandler;

  @Mock private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    lenient().when(request.getRequestURI()).thenReturn("/test/path");
    lenient().when(request.getMethod()).thenReturn("GET");
  }

  @Test
  void testHandleIllegalArgumentException() {
    // Given
    IllegalArgumentException exception = new IllegalArgumentException("参数无效");

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleIllegalArgumentException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("参数无效", response.getMessage());
    assertNull(response.getData());
  }

  @Test
  void testHandleIllegalArgumentExceptionWithNullMessage() {
    // Given
    IllegalArgumentException exception = new IllegalArgumentException();

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleIllegalArgumentException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertNull(response.getData());
  }

  @Test
  void testHandleBusinessException() {
    // Given
    BusinessException exception = new BusinessException("业务异常");

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleBusinessException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("业务异常", response.getMessage());
  }

  @Test
  void testHandleBusinessExceptionWithCode() {
    // Given
    BusinessException exception = new BusinessException("CUSTOM_ERROR", "自定义业务异常");

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleBusinessException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("自定义业务异常", response.getMessage());
    assertNull(response.getData());
  }

  @Test
  void testHandleBusinessExceptionWithNullCode() {
    // Given
    BusinessException exception = new BusinessException(null, "业务异常消息");

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleBusinessException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("业务异常消息", response.getMessage());
  }

  @Test
  void testHandleBusinessExceptionWithCause() {
    // Given
    Throwable cause = new RuntimeException("根本原因");
    BusinessException exception = new BusinessException("SYSTEM_ERROR", "系统异常", cause);

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleBusinessException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("系统异常", response.getMessage());
    assertNull(response.getData());
  }

  @Test
  void testHandleRuntimeException() {
    // Given
    RuntimeException exception = new RuntimeException("运行时异常");

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleRuntimeException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("系统运行异常: 运行时异常", response.getMessage());
    assertNull(response.getData());
  }

  @Test
  void testHandleRuntimeExceptionWithNullMessage() {
    // Given
    RuntimeException exception = new RuntimeException();

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleRuntimeException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertTrue(response.getMessage().startsWith("系统运行异常:"));
    assertNull(response.getData());
  }

  @Test
  void testHandleNullPointerException() {
    // Given
    NullPointerException exception = new NullPointerException("空指针异常");

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleNullPointerException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("系统内部错误，请联系管理员", response.getMessage());
    assertNull(response.getData());
  }

  @Test
  void testHandleNullPointerExceptionWithNullMessage() {
    // Given
    NullPointerException exception = new NullPointerException();

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleNullPointerException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("系统内部错误，请联系管理员", response.getMessage());
    assertNull(response.getData());
  }

  @Test
  void testHandleGeneralException() {
    // Given
    Exception exception = new Exception("通用异常");

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleGeneralException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("系统错误，请联系管理员", response.getMessage());
    assertNull(response.getData());
  }

  @Test
  void testHandleGeneralExceptionWithMethod() {
    // Given
    when(request.getMethod()).thenReturn("POST");
    Exception exception = new Exception("通用异常测试");

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleGeneralException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("系统错误，请联系管理员", response.getMessage());
    assertNull(response.getData());

    // 验证日志记录了请求方法
    verify(request, atLeastOnce()).getMethod();
  }

  @Test
  void testHandleTypeMismatchException() {
    // Given
    MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
    when(exception.getName()).thenReturn("id");
    when(exception.getRequiredType()).thenReturn((Class) Long.class);

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleTypeMismatchException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertTrue(response.getMessage().contains("参数"));
    assertTrue(response.getMessage().contains("id"));
    assertTrue(response.getMessage().contains("Long"));
    assertNull(response.getData());
  }

  @Test
  void testHandleTypeMismatchExceptionWithNullRequiredType() {
    // Given
    MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
    when(exception.getName()).thenReturn("testParam");
    when(exception.getRequiredType()).thenReturn(null);

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleTypeMismatchException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertTrue(response.getMessage().contains("testParam"));
    assertTrue(response.getMessage().contains("unknown"));
  }

  @Test
  void testHandleTypeMismatchExceptionWithComplexType() {
    // Given
    MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
    when(exception.getName()).thenReturn("complexParam");
    when(exception.getRequiredType()).thenReturn((Class) java.util.List.class);

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleTypeMismatchException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertTrue(response.getMessage().contains("complexParam"));
    assertTrue(response.getMessage().contains("List"));
  }

  @Test
  void testHandleMethodArgumentNotValidException() throws NoSuchMethodException {
    // Given
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError1 =
        new FieldError("testObject", "field1", null, false, null, null, "Field1 error");
    FieldError fieldError2 =
        new FieldError("testObject", "field2", null, false, null, null, "Field2 error");

    when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

    // 创建一个真实的MethodParameter
    Method method = this.getClass().getDeclaredMethod("testHandleMethodArgumentNotValidException");
    MethodParameter methodParameter = new MethodParameter(method, -1);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleValidationException(exception, request);

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
  void testHandleValidationExceptionEmptyErrors() throws NoSuchMethodException {
    // Given
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());

    Method method = this.getClass().getDeclaredMethod("testHandleValidationExceptionEmptyErrors");
    MethodParameter methodParameter = new MethodParameter(method, -1);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleValidationException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("参数验证失败", response.getMessage());
    assertNotNull(response.getData());
    assertTrue(response.getData() instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, String> errorMap = (Map<String, String>) response.getData();
    assertTrue(errorMap.isEmpty());
  }

  @Test
  void testHandleValidationExceptionWithMultipleFieldErrors() throws NoSuchMethodException {
    // Given
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError1 = new FieldError("user", "name", null, false, null, null, "姓名不能为空");
    FieldError fieldError2 = new FieldError("user", "email", null, false, null, null, "邮箱格式不正确");
    FieldError fieldError3 = new FieldError("user", "age", null, false, null, null, "年龄必须大于0");

    when(bindingResult.getAllErrors())
        .thenReturn(Arrays.asList(fieldError1, fieldError2, fieldError3));

    Method method =
        this.getClass().getDeclaredMethod("testHandleValidationExceptionWithMultipleFieldErrors");
    MethodParameter methodParameter = new MethodParameter(method, -1);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    // When
    ApiResponse<Object> response =
        globalExceptionHandler.handleValidationException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("参数验证失败", response.getMessage());
    assertNotNull(response.getData());

    assertTrue(response.getData() instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, String> errorMap = (Map<String, String>) response.getData();
    assertEquals(3, errorMap.size());
    assertEquals("姓名不能为空", errorMap.get("name"));
    assertEquals("邮箱格式不正确", errorMap.get("email"));
    assertEquals("年龄必须大于0", errorMap.get("age"));
  }

  @Test
  void testHandleBindException() {
    // Given
    FieldError fieldError =
        new FieldError("testObject", "testField", null, false, null, null, "Test error");
    BindException exception = new BindException(new Object(), "testObject");
    exception.addError(fieldError);

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
  void testHandleBindExceptionEmptyErrors() {
    // Given
    BindException exception = new BindException(new Object(), "testObject");

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

  @Test
  void testHandleBindExceptionWithMultipleFieldErrors() {
    // Given
    FieldError fieldError1 = new FieldError("form", "username", null, false, null, null, "用户名已存在");
    FieldError fieldError2 = new FieldError("form", "password", null, false, null, null, "密码长度不足");

    BindException exception = new BindException(new Object(), "form");
    exception.addError(fieldError1);
    exception.addError(fieldError2);

    // When
    ApiResponse<Object> response = globalExceptionHandler.handleBindException(exception, request);

    // Then
    assertNotNull(response);
    assertFalse(response.isSuccess());
    assertEquals("参数绑定失败", response.getMessage());
    assertNotNull(response.getData());

    assertTrue(response.getData() instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, String> errorMap = (Map<String, String>) response.getData();
    assertEquals(2, errorMap.size());
    assertEquals("用户名已存在", errorMap.get("username"));
    assertEquals("密码长度不足", errorMap.get("password"));
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
  void testAllExceptionHandlersLogCorrectly() {
    // 测试所有异常处理器都正确记录了请求URI

    // Test IllegalArgumentException logging
    IllegalArgumentException illegalArgException = new IllegalArgumentException("参数错误");
    globalExceptionHandler.handleIllegalArgumentException(illegalArgException, request);

    // Test BusinessException logging
    BusinessException businessException = new BusinessException("业务错误");
    globalExceptionHandler.handleBusinessException(businessException, request);

    // Test RuntimeException logging
    RuntimeException runtimeException = new RuntimeException("运行时错误");
    globalExceptionHandler.handleRuntimeException(runtimeException, request);

    // Test NullPointerException logging
    NullPointerException nullPointerException = new NullPointerException("空指针错误");
    globalExceptionHandler.handleNullPointerException(nullPointerException, request);

    // Test general Exception logging
    Exception generalException = new Exception("通用错误");
    globalExceptionHandler.handleGeneralException(generalException, request);

    // 验证request.getRequestURI()被调用了多次
    verify(request, atLeast(5)).getRequestURI();
  }
}
