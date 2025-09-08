package com.example.service.chat;

import com.example.dto.common.ModelInfo;
import com.example.dto.common.UserModelPreferenceDto;
import com.example.service.ModelManagementService;
import com.example.service.provider.ModelProvider;
import com.example.service.provider.ModelProviderManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * DefaultModelSelector测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class DefaultModelSelectorTest {

    @Mock
    private ModelProviderManager providerManager;

    @Mock
    private ModelManagementService modelManagementService;

    @Mock
    private ModelProvider defaultProvider;

    @Mock
    private ModelProvider specifiedProvider;

    private DefaultModelSelector modelSelector;

    @BeforeEach
    void setUp() {
        modelSelector = new DefaultModelSelector(providerManager, modelManagementService);
    }

    @Test
    void shouldGetModelProviderWithSpecifiedName() {
        // Given
        String providerName = "DeepSeek";
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);

        // When
        ModelProvider provider = modelSelector.getModelProvider(providerName);

        // Then
        assertThat(provider).isEqualTo(specifiedProvider);
        verify(providerManager).getProvider(providerName);
    }

    @Test
    void shouldGetDefaultModelProviderWithNullName() {
        // Given
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);

        // When
        ModelProvider provider = modelSelector.getModelProvider(null);

        // Then
        assertThat(provider).isEqualTo(defaultProvider);
        verify(providerManager).getDefaultProvider();
    }

    @Test
    void shouldGetDefaultModelProviderWithEmptyName() {
        // Given
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);

        // When
        ModelProvider provider = modelSelector.getModelProvider("");

        // Then
        assertThat(provider).isEqualTo(defaultProvider);
        verify(providerManager).getDefaultProvider();
    }

    @Test
    void shouldGetDefaultModelProviderWithWhitespaceName() {
        // Given
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);

        // When
        ModelProvider provider = modelSelector.getModelProvider("   ");

        // Then
        assertThat(provider).isEqualTo(defaultProvider);
        verify(providerManager).getDefaultProvider();
    }

    @Test
    void shouldGetActualModelNameWithValidModel() {
        // Given
        ModelInfo model1 = new ModelInfo();
        model1.setName("deepseek-chat");
        
        ModelInfo model2 = new ModelInfo();
        model2.setName("deepseek-coder");
        
        List<ModelInfo> availableModels = Arrays.asList(model1, model2);
        when(specifiedProvider.getAvailableModels()).thenReturn(availableModels);

        // When
        String actualModelName = modelSelector.getActualModelName(specifiedProvider, "deepseek-chat");

        // Then
        assertThat(actualModelName).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldGetActualModelNameWithInvalidModel() {
        // Given
        ModelInfo model1 = new ModelInfo();
        model1.setName("deepseek-chat");
        
        ModelInfo model2 = new ModelInfo();
        model2.setName("deepseek-coder");
        
        List<ModelInfo> availableModels = Arrays.asList(model1, model2);
        when(specifiedProvider.getAvailableModels()).thenReturn(availableModels);

        // When
        String actualModelName = modelSelector.getActualModelName(specifiedProvider, "invalid-model");

        // Then
        assertThat(actualModelName).isEqualTo("deepseek-chat"); // First available model
    }

    @Test
    void shouldGetActualModelNameWithNullModel() {
        // Given
        ModelInfo model1 = new ModelInfo();
        model1.setName("deepseek-chat");
        
        List<ModelInfo> availableModels = Arrays.asList(model1);
        when(specifiedProvider.getAvailableModels()).thenReturn(availableModels);

        // When
        String actualModelName = modelSelector.getActualModelName(specifiedProvider, null);

        // Then
        assertThat(actualModelName).isEqualTo("deepseek-chat"); // First available model
    }

    @Test
    void shouldGetActualModelNameWithEmptyModel() {
        // Given
        ModelInfo model1 = new ModelInfo();
        model1.setName("deepseek-chat");
        
        List<ModelInfo> availableModels = Arrays.asList(model1);
        when(specifiedProvider.getAvailableModels()).thenReturn(availableModels);

        // When
        String actualModelName = modelSelector.getActualModelName(specifiedProvider, "");

        // Then
        assertThat(actualModelName).isEqualTo("deepseek-chat"); // First available model
    }

    @Test
    void shouldGetActualModelNameWithWhitespaceModel() {
        // Given
        ModelInfo model1 = new ModelInfo();
        model1.setName("deepseek-chat");
        
        List<ModelInfo> availableModels = Arrays.asList(model1);
        when(specifiedProvider.getAvailableModels()).thenReturn(availableModels);

        // When
        String actualModelName = modelSelector.getActualModelName(specifiedProvider, "   ");

        // Then
        assertThat(actualModelName).isEqualTo("deepseek-chat"); // First available model
    }

    @Test
    void shouldThrowExceptionWhenNoAvailableModels() {
        // Given
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> modelSelector.getActualModelName(specifiedProvider, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("没有可用的模型");
    }

    @Test
    void shouldSelectModelWithExplicitProviderAndModel() {
        // Given
        Long userId = 1L;
        String providerName = "DeepSeek";
        String modelName = "deepseek-chat";
        
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithUserPreference() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = null;
        
        UserModelPreferenceDto userPreference = new UserModelPreferenceDto();
        userPreference.setProviderName("DeepSeek");
        userPreference.setModelName("deepseek-chat");
        
        when(modelManagementService.getUserDefaultModel(userId)).thenReturn(userPreference);
        when(providerManager.getProvider("DeepSeek")).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithDefaultWhenNoUserPreference() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = null;
        
        when(modelManagementService.getUserDefaultModel(userId)).thenReturn(null);
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithDefaultWhenUserPreferenceException() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = null;
        
        when(modelManagementService.getUserDefaultModel(userId)).thenThrow(new RuntimeException("Database error"));
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithDefaultWhenInvalidUserPreference() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = null;
        
        UserModelPreferenceDto userPreference = new UserModelPreferenceDto();
        userPreference.setProviderName("InvalidProvider");
        userPreference.setModelName("invalid-model");
        
        when(modelManagementService.getUserDefaultModel(userId)).thenReturn(userPreference);
        when(providerManager.getProvider("InvalidProvider")).thenReturn(specifiedProvider);
        
        // No available models for the specified provider
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList());

        // Should fall back to default provider
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        ModelInfo defaultModel = new ModelInfo();
        defaultModel.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(defaultModel));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithOnlyProviderName() {
        // Given
        Long userId = 1L;
        String providerName = "DeepSeek";
        String modelName = null;
        
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithOnlyModelName() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = "deepseek-chat";
        
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo model1 = new ModelInfo();
        model1.setName("deepseek-coder");
        
        ModelInfo model2 = new ModelInfo();
        model2.setName("deepseek-chat");
        
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(model1, model2));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithInvalidProviderName() {
        // Given
        Long userId = 1L;
        String providerName = "InvalidProvider";
        String modelName = null;
        
        when(providerManager.getProvider(providerName)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> modelSelector.selectModelForUser(userId, providerName, modelName))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldSelectModelWithNullUserId() {
        // Given
        Long userId = null;
        String providerName = null;
        String modelName = null;
        
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithEmptyUserPreference() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = null;
        
        UserModelPreferenceDto userPreference = new UserModelPreferenceDto();
        userPreference.setProviderName(null);
        userPreference.setModelName(null);
        
        when(modelManagementService.getUserDefaultModel(userId)).thenReturn(userPreference);
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldGetModelProviderWithSpecialCharacters() {
        // Given
        String providerName = "特殊-provider🌟";
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);

        // When
        ModelProvider provider = modelSelector.getModelProvider(providerName);

        // Then
        assertThat(provider).isEqualTo(specifiedProvider);
        verify(providerManager).getProvider(providerName);
    }

    @Test
    void shouldGetModelProviderWithLongName() {
        // Given
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
          longName.append("long-provider-name");
        }
        String providerName = longName.toString();
        
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);

        // When
        ModelProvider provider = modelSelector.getModelProvider(providerName);

        // Then
        assertThat(provider).isEqualTo(specifiedProvider);
        verify(providerManager).getProvider(providerName);
    }

    @Test
    void shouldGetActualModelNameWithSpecialCharacters() {
        // Given
        ModelInfo model1 = new ModelInfo();
        model1.setName("特殊-model🌟");
        
        ModelInfo model2 = new ModelInfo();
        model2.setName("another-model");
        
        List<ModelInfo> availableModels = Arrays.asList(model1, model2);
        when(specifiedProvider.getAvailableModels()).thenReturn(availableModels);

        // When
        String actualModelName = modelSelector.getActualModelName(specifiedProvider, "特殊-model🌟");

        // Then
        assertThat(actualModelName).isEqualTo("特殊-model🌟");
    }

    @Test
    void shouldGetActualModelNameWithLongName() {
        // Given
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
          longName.append("long-model-name");
        }
        String modelName = longName.toString();
        
        ModelInfo model1 = new ModelInfo();
        model1.setName(modelName);
        
        ModelInfo model2 = new ModelInfo();
        model2.setName("another-model");
        
        List<ModelInfo> availableModels = Arrays.asList(model1, model2);
        when(specifiedProvider.getAvailableModels()).thenReturn(availableModels);

        // When
        String actualModelName = modelSelector.getActualModelName(specifiedProvider, modelName);

        // Then
        assertThat(actualModelName).isEqualTo(modelName);
    }

    @Test
    void shouldSelectModelWithSpecialCharacters() {
        // Given
        Long userId = 1L;
        String providerName = "特殊-provider🌟";
        String modelName = "特殊-model🌟";
        
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("特殊-model🌟");
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("特殊-model🌟");
    }

    @Test
    void shouldSelectModelWithLongValues() {
        // Given
        Long userId = 1L;
        StringBuilder longProviderName = new StringBuilder();
        StringBuilder longModelName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
          longProviderName.append("long-provider-name");
          longModelName.append("long-model-name");
        }
        String providerName = longProviderName.toString();
        String modelName = longModelName.toString();
        
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName(modelName);
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo(modelName);
    }

    @Test
    void shouldSelectModelWithUnicode() {
        // Given
        Long userId = 1L;
        String providerName = "Unicode提供者测试";
        String modelName = "Unicode模型测试";
        
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("Unicode模型测试");
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("Unicode模型测试");
    }

    @Test
    void shouldGetModelProviderWithExceptionInProviderManager() {
        // Given
        String providerName = "exception-provider";
        when(providerManager.getProvider(providerName)).thenThrow(new RuntimeException("Provider manager error"));

        // When & Then
        assertThatThrownBy(() -> modelSelector.getModelProvider(providerName))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Provider manager error");
    }

    @Test
    void shouldGetActualModelNameWithExceptionInProvider() {
        // Given
        when(specifiedProvider.getAvailableModels()).thenThrow(new RuntimeException("Provider error"));

        // When & Then
        assertThatThrownBy(() -> modelSelector.getActualModelName(specifiedProvider, "any-model"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Provider error");
    }

    @Test
    void shouldSelectModelWithZeroUserId() {
        // Given
        Long userId = 0L;
        String providerName = null;
        String modelName = null;
        
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithNegativeUserId() {
        // Given
        Long userId = -1L;
        String providerName = null;
        String modelName = null;
        
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithWhitespaceProviderName() {
        // Given
        Long userId = 1L;
        String providerName = "   ";
        String modelName = null;
        
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithWhitespaceModelName() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = "   ";
        
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldGetActualModelNameWithMultipleMatchingModels() {
        // Given
        ModelInfo model1 = new ModelInfo();
        model1.setName("deepseek-chat");
        
        ModelInfo model2 = new ModelInfo();
        model2.setName("deepseek-chat"); // 同名模型
    
        ModelInfo model3 = new ModelInfo();
        model3.setName("deepseek-coder");
        
        List<ModelInfo> availableModels = Arrays.asList(model1, model2, model3);
        when(specifiedProvider.getAvailableModels()).thenReturn(availableModels);

        // When
        String actualModelName = modelSelector.getActualModelName(specifiedProvider, "deepseek-chat");

        // Then
        assertThat(actualModelName).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithNullProviderAndValidModel() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = "deepseek-chat";
        
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo model1 = new ModelInfo();
        model1.setName("deepseek-coder");
        
        ModelInfo model2 = new ModelInfo();
        model2.setName("deepseek-chat");
        
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(model1, model2));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithValidProviderAndNullModel() {
        // Given
        Long userId = 1L;
        String providerName = "DeepSeek";
        String modelName = null;
        
        when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithBothNull() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = null;
        
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(modelInfo));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldGetModelProviderWithProviderManagerExceptionAndFallback() {
        // Given
        String providerName = "test-provider";
        when(providerManager.getProvider(providerName)).thenThrow(new RuntimeException("Provider error"));
    
        // 注意：这个测试用例实际上不会触发异常，因为getModelProvider方法不会捕获异常并回退到默认提供者
        // 这里是为了测试异常情况下的行为

        // When & Then
        assertThatThrownBy(() -> modelSelector.getModelProvider(providerName))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Provider error");
    }

    @Test
    void shouldSelectModelWithUserPreferenceAndProviderException() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = null;
        
        UserModelPreferenceDto userPreference = new UserModelPreferenceDto();
        userPreference.setProviderName("ExceptionProvider");
        userPreference.setModelName("exception-model");
        
        when(modelManagementService.getUserDefaultModel(userId)).thenReturn(userPreference);
        when(providerManager.getProvider("ExceptionProvider")).thenThrow(new RuntimeException("Provider error"));
    
        // Should fall back to default provider
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        ModelInfo defaultModel = new ModelInfo();
        defaultModel.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(defaultModel));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldSelectModelWithUserPreferenceAndModelException() {
        // Given
        Long userId = 1L;
        String providerName = null;
        String modelName = null;
        
        UserModelPreferenceDto userPreference = new UserModelPreferenceDto();
        userPreference.setProviderName("DeepSeek");
        userPreference.setModelName("invalid-model");
        
        when(modelManagementService.getUserDefaultModel(userId)).thenReturn(userPreference);
        when(providerManager.getProvider("DeepSeek")).thenReturn(specifiedProvider);
    
        // Make all models unavailable
        when(specifiedProvider.getAvailableModels()).thenReturn(Arrays.asList());

        // Should fall back to default provider
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        ModelInfo defaultModel = new ModelInfo();
        defaultModel.setName("deepseek-chat");
        when(defaultProvider.getAvailableModels()).thenReturn(Arrays.asList(defaultModel));

        // When
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(userId, providerName, modelName);

        // Then
        assertThat(selection).isNotNull();
        assertThat(selection.provider()).isEqualTo(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
    }
}