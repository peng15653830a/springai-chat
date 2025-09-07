package com.example.service.chat;

import com.example.dto.response.SseEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 聊天错误处理器单元测试
 */
class ChatErrorHandlerTest {

    private DefaultChatErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new DefaultChatErrorHandler();
    }

    @Test
    void testHandleChatError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Test error");

        // 执行测试
        Flux<SseEventResponse> errorResponse = errorHandler.handleChatError(error);

        // 验证结果
        StepVerifier.create(errorResponse)
                .expectNextMatches(response -> {
                    assertThat(response.getType()).isEqualTo("error");
                    assertThat(response.getData()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testHandleChatErrorWithNullError() {
        // 执行测试
        Flux<SseEventResponse> errorResponse = errorHandler.handleChatError(null);

        // 验证结果
        StepVerifier.create(errorResponse)
                .expectNextMatches(response -> {
                    assertThat(response.getType()).isEqualTo("error");
                    assertThat(response.getData()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testGetErrorMessageWithNullError() {
        // 执行测试
        String message = errorHandler.getErrorMessage(null);

        // 验证结果
        assertThat(message).isEqualTo("未知错误");
    }

    @Test
    void testGetErrorMessageWithNetworkError() {
        // 准备测试数据
        Throwable error = new ConnectException("Connection refused");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("网络连接异常，请检查网络设置后重试");
    }

    @Test
    void testGetErrorMessageWithUnknownHostError() {
        // 准备测试数据
        Throwable error = new UnknownHostException("Unknown host");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("网络连接异常，请检查网络设置后重试");
    }

    @Test
    void testGetErrorMessageWithTimeoutError() {
        // 准备测试数据
        Throwable error = new TimeoutException("Request timeout");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("请求超时，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithSocketTimeoutError() {
        // 准备测试数据
        Throwable error = new SocketTimeoutException("Socket timeout");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("请求超时，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithApiKeyError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Invalid api key");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("API密钥配置错误，请联系管理员");
    }

    @Test
    void testGetErrorMessageWithQuotaExceededError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Rate limit exceeded");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("API调用配额已用完，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithModelUnavailableError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Model not found");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithUnknownError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Unknown error");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testIsRetryableErrorWithNetworkError() {
        // 准备测试数据
        Throwable error = new ConnectException("Connection refused");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithTimeoutError() {
        // 准备测试数据
        Throwable error = new SocketTimeoutException("Socket timeout");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithInternalError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Internal error");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithApiKeyError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Invalid api key");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isFalse();
    }

    @Test
    void testIsRetryableErrorWithQuotaExceededError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Rate limit exceeded");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isFalse();
    }

    @Test
    void testIsRetryableErrorWithModelUnavailableError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Model not found");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithUnknownError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Unknown error");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue(); // 根据实现，未知错误被归类为INTERNAL_ERROR，是可重试的
    }

    @Test
    void testIsRetryableErrorWithNullError() {
        // 执行测试
        boolean retryable = errorHandler.isRetryableError(null);

        // 验证结果
        assertThat(retryable).isFalse();
    }

    @Test
    void testGetErrorTypeWithNullError() {
        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(null);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.UNKNOWN_ERROR);
    }

    @Test
    void testGetErrorTypeWithConnectException() {
        // 准备测试数据
        Throwable error = new ConnectException("Connection refused");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.NETWORK_ERROR);
    }

    @Test
    void testGetErrorTypeWithUnknownHostException() {
        // 准备测试数据
        Throwable error = new UnknownHostException("Unknown host");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.NETWORK_ERROR);
    }

    @Test
    void testGetErrorTypeWithTimeoutException() {
        // 准备测试数据
        Throwable error = new TimeoutException("Request timeout");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.TIMEOUT_ERROR);
    }

    @Test
    void testGetErrorTypeWithSocketTimeoutException() {
        // 准备测试数据
        Throwable error = new SocketTimeoutException("Socket timeout");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.TIMEOUT_ERROR);
    }

    @Test
    void testGetErrorTypeWithApiKeyError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Invalid api key");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.API_KEY_ERROR);
    }

    @Test
    void testGetErrorTypeWithQuotaExceededError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Rate limit exceeded");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.QUOTA_EXCEEDED);
    }

    @Test
    void testGetErrorTypeWithModelUnavailableError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Model not found");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorTypeWithUnknownError() {
        // 准备测试数据
        Throwable error = new RuntimeException("Unknown error");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorTypeWithErrorMessageContainingUnauthorized() {
        // 准备测试数据
        Throwable error = new RuntimeException("Unauthorized access");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorTypeWithErrorMessageContainingNotFound() {
        // 准备测试数据
        Throwable error = new RuntimeException("Model not found");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorTypeWithErrorMessageContainingInvalid() {
        // 准备测试数据
        Throwable error = new RuntimeException("Invalid request");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorMessageWithSpecialCharacters() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊字符错误🌟🔍🚀");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithLongMessage() {
        // 准备测试数据
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("长错误消息");
        }
        Throwable error = new RuntimeException(longMessage.toString());

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithEmptyMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithWhitespaceMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("   \t\n   ");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithNullMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException((String) null);

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testIsRetryableErrorWithSpecialCharacters() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊字符错误🌟🔍🚀");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithLongMessage() {
        // 准备测试数据
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("长错误消息");
        }
        Throwable error = new RuntimeException(longMessage.toString());

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithEmptyMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithWhitespaceMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("   \t\n   ");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testGetErrorTypeWithSpecialCharacters() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊字符错误🌟🔍🚀");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorTypeWithLongMessage() {
        // 准备测试数据
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("长错误消息");
        }
        Throwable error = new RuntimeException(longMessage.toString());

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorTypeWithEmptyMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorTypeWithWhitespaceMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("   \t\n   ");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testHandleChatErrorWithSpecialCharacters() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊字符错误🌟🔍🚀");

        // 执行测试
        Flux<SseEventResponse> errorResponse = errorHandler.handleChatError(error);

        // 验证结果
        StepVerifier.create(errorResponse)
                .expectNextMatches(response -> {
                    assertThat(response.getType()).isEqualTo("error");
                    assertThat(response.getData()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testHandleChatErrorWithLongMessage() {
        // 准备测试数据
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("长错误消息");
        }
        Throwable error = new RuntimeException(longMessage.toString());

        // 执行测试
        Flux<SseEventResponse> errorResponse = errorHandler.handleChatError(error);

        // 验证结果
        StepVerifier.create(errorResponse)
                .expectNextMatches(response -> {
                    assertThat(response.getType()).isEqualTo("error");
                    assertThat(response.getData()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testGetErrorMessageWithNestedException() {
        // 准备测试数据
        Throwable cause = new RuntimeException("根本原因");
        Throwable error = new RuntimeException("包装异常", cause);

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testIsRetryableErrorWithNestedException() {
        // 准备测试数据
        Throwable cause = new RuntimeException("根本原因");
        Throwable error = new RuntimeException("包装异常", cause);

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testGetErrorTypeWithNestedException() {
        // 准备测试数据
        Throwable cause = new RuntimeException("根本原因");
        Throwable error = new RuntimeException("包装异常", cause);

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }

    @Test
    void testGetErrorMessageWithConnectExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new ConnectException("特殊连接错误消息");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("网络连接异常，请检查网络设置后重试");
    }

    @Test
    void testGetErrorMessageWithUnknownHostExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new UnknownHostException("特殊未知主机错误消息");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("网络连接异常，请检查网络设置后重试");
    }

    @Test
    void testGetErrorMessageWithTimeoutExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new TimeoutException("特殊超时错误消息");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("请求超时，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithSocketTimeoutExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new SocketTimeoutException("特殊套接字超时错误消息");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("请求超时，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithApiKeyErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊API密钥错误消息：Invalid api key");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("API密钥配置错误，请联系管理员");
    }

    @Test
    void testGetErrorMessageWithQuotaExceededErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊配额超限错误消息：Rate limit exceeded");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("API调用配额已用完，请稍后重试");
    }

    @Test
    void testGetErrorMessageWithModelUnavailableErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊模型不可用错误消息：Model not found");

        // 执行测试
        String message = errorHandler.getErrorMessage(error);

        // 验证结果
        assertThat(message).isEqualTo("系统内部错误，请稍后重试");
    }

    @Test
    void testIsRetryableErrorWithConnectExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new ConnectException("特殊连接错误消息");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithUnknownHostExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new UnknownHostException("特殊未知主机错误消息");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithTimeoutExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new TimeoutException("特殊超时错误消息");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithSocketTimeoutExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new SocketTimeoutException("特殊套接字超时错误消息");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testIsRetryableErrorWithApiKeyErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊API密钥错误消息：Invalid api key");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isFalse();
    }

    @Test
    void testIsRetryableErrorWithQuotaExceededErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊配额超限错误消息：Rate limit exceeded");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isFalse();
    }

    @Test
    void testIsRetryableErrorWithModelUnavailableErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊模型不可用错误消息：Model not found");

        // 执行测试
        boolean retryable = errorHandler.isRetryableError(error);

        // 验证结果
        assertThat(retryable).isTrue();
    }

    @Test
    void testGetErrorTypeWithConnectExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new ConnectException("特殊连接错误消息");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.NETWORK_ERROR);
    }

    @Test
    void testGetErrorTypeWithUnknownHostExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new UnknownHostException("特殊未知主机错误消息");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.NETWORK_ERROR);
    }

    @Test
    void testGetErrorTypeWithTimeoutExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new TimeoutException("特殊超时错误消息");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.TIMEOUT_ERROR);
    }

    @Test
    void testGetErrorTypeWithSocketTimeoutExceptionSpecialMessage() {
        // 准备测试数据
        Throwable error = new SocketTimeoutException("特殊套接字超时错误消息");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.TIMEOUT_ERROR);
    }

    @Test
    void testGetErrorTypeWithApiKeyErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊API密钥错误消息：Invalid api key");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.API_KEY_ERROR);
    }

    @Test
    void testGetErrorTypeWithQuotaExceededErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊配额超限错误消息：Rate limit exceeded");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.QUOTA_EXCEEDED);
    }

    @Test
    void testGetErrorTypeWithModelUnavailableErrorSpecialMessage() {
        // 准备测试数据
        Throwable error = new RuntimeException("特殊模型不可用错误消息：Model not found");

        // 执行测试
        ChatErrorHandler.ErrorType errorType = errorHandler.getErrorType(error);

        // 验证结果
        assertThat(errorType).isEqualTo(ChatErrorHandler.ErrorType.INTERNAL_ERROR);
    }
}