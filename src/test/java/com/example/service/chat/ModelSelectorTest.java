package com.example.service.chat;

import com.example.dto.common.ModelInfo;
import com.example.dto.common.UserModelPreferenceDto;
import com.example.service.ModelManagementService;
import com.example.service.provider.ModelProvider;
import com.example.service.provider.ModelProviderManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * ModelSelector测试类
 *
 * @author xupeng
 */
@ExtendWith(MockitoExtension.class)
class ModelSelectorTest {

    @Mock
    private ModelProvider defaultProvider;

    @Mock
    private ModelProvider specifiedProvider;

    @Mock
    private ModelManagementService modelManagementService;

    @Mock
    private ModelProviderManager providerManager;

    @Test
    void testGetModelProviderWithSpecifiedProvider() {
        // 准备测试数据
        when(providerManager.getProvider("deepseek")).thenReturn(specifiedProvider);

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelProvider provider = modelSelector.getModelProvider("deepseek");

        // 验证结果
        assertThat(provider).isSameAs(specifiedProvider);
        verify(providerManager).getProvider("deepseek");
    }

    @Test
    void testGetModelProviderWithDefaultProvider() {
        // 准备测试数据
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelProvider provider = modelSelector.getModelProvider(null);

        // 验证结果
        assertThat(provider).isSameAs(defaultProvider);
        verify(providerManager).getDefaultProvider();
    }

    @Test
    void testGetActualModelNameWithValidModel() {
        // 准备测试数据
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        modelInfo.setAvailable(true);
        
        when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        String modelName = modelSelector.getActualModelName(specifiedProvider, "deepseek-chat");

        // 验证结果
        assertThat(modelName).isEqualTo("deepseek-chat");
        verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testGetActualModelNameWithInvalidModel() {
        // 准备测试数据
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        modelInfo.setAvailable(true);
        
        when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        String modelName = modelSelector.getActualModelName(specifiedProvider, "invalid-model");

        // 验证结果
        assertThat(modelName).isEqualTo("deepseek-chat");
        verify(specifiedProvider, times(2)).getAvailableModels(); // 会调用两次
    }

    @Test
    void testGetActualModelNameWithEmptyModelName() {
        // 准备测试数据
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        modelInfo.setAvailable(true);
        
        when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        String modelName = modelSelector.getActualModelName(specifiedProvider, "");

        // 验证结果
        assertThat(modelName).isEqualTo("deepseek-chat");
        verify(specifiedProvider).getAvailableModels(); // 只调用一次
    }

    @Test
    void testGetActualModelNameWithNoAvailableModels() {
        // 准备测试数据
        when(specifiedProvider.getAvailableModels()).thenReturn(List.of());
        when(specifiedProvider.getProviderName()).thenReturn("deepseek");

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 验证异常
        assertThatThrownBy(() -> modelSelector.getActualModelName(specifiedProvider, "any-model"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("提供者 deepseek 没有可用的模型");
        verify(specifiedProvider, times(2)).getAvailableModels(); // 会调用两次
    }

    @Test
    void testSelectModelForUserWithSpecifiedProviderAndModel() {
        // 准备测试数据
        when(providerManager.getProvider("deepseek")).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        modelInfo.setAvailable(true);
        when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(
                1L, "deepseek", "deepseek-chat");

        // 验证结果
        assertThat(selection.provider()).isSameAs(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
        verify(providerManager).getProvider("deepseek");
        verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithUserPreference() {
        // 准备测试数据
        UserModelPreferenceDto userPreference = new UserModelPreferenceDto();
        userPreference.setProviderName("deepseek");
        userPreference.setModelName("deepseek-chat");
        
        when(modelManagementService.getUserDefaultModel(1L)).thenReturn(userPreference);
        when(providerManager.getProvider("deepseek")).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        modelInfo.setAvailable(true);
        when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(1L, null, null);

        // 验证结果
        assertThat(selection.provider()).isSameAs(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
        verify(modelManagementService).getUserDefaultModel(1L);
        verify(providerManager).getProvider("deepseek");
        verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithDefaultSelection() {
        // 准备测试数据
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("default-model");
        modelInfo.setAvailable(true);
        when(defaultProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(null, null, null);

        // 验证结果
        assertThat(selection.provider()).isSameAs(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("default-model");
        verify(providerManager).getDefaultProvider();
        verify(defaultProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithUserPreferenceException() {
        // 准备测试数据
        when(modelManagementService.getUserDefaultModel(1L)).thenThrow(new RuntimeException("DB error"));
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("default-model");
        modelInfo.setAvailable(true);
        when(defaultProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(1L, null, null);

        // 验证结果
        assertThat(selection.provider()).isSameAs(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("default-model");
        verify(modelManagementService).getUserDefaultModel(1L);
        verify(providerManager).getDefaultProvider();
        verify(defaultProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithNullUserId() {
        // 准备测试数据
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("default-model");
        modelInfo.setAvailable(true);
        when(defaultProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(null, null, null);

        // 验证结果
        assertThat(selection.provider()).isSameAs(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("default-model");
        verify(providerManager).getDefaultProvider();
        verify(defaultProvider).getAvailableModels();
    }

    @Test
    void testGetModelProviderWithEmptyProviderName() {
        // 准备测试数据
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelProvider provider = modelSelector.getModelProvider("");

        // 验证结果
        assertThat(provider).isSameAs(defaultProvider);
        verify(providerManager).getDefaultProvider();
    }

    @Test
    void testGetActualModelNameWithNullModelName() {
        // 准备测试数据
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        modelInfo.setAvailable(true);
        
        when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        String modelName = modelSelector.getActualModelName(specifiedProvider, null);

        // 验证结果
        assertThat(modelName).isEqualTo("deepseek-chat");
        verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithOnlyProviderName() {
        // 准备测试数据
        when(providerManager.getProvider("deepseek")).thenReturn(specifiedProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("deepseek-chat");
        modelInfo.setAvailable(true);
        when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(1L, "deepseek", null);

        // 验证结果
        assertThat(selection.provider()).isSameAs(specifiedProvider);
        assertThat(selection.modelName()).isEqualTo("deepseek-chat");
        verify(providerManager).getProvider("deepseek");
        verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithOnlyModelName() {
        // 准备测试数据
        when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("default-model");
        modelInfo.setAvailable(true);
        when(defaultProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

        ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

        // 执行测试
        ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(1L, null, "some-model");

        // 验证结果
        assertThat(selection.provider()).isSameAs(defaultProvider);
        assertThat(selection.modelName()).isEqualTo("default-model");
        verify(providerManager).getDefaultProvider();
        // 由于在getActualModelName方法中会调用两次getAvailableModels，我们需要验证被调用两次
        verify(defaultProvider, times(2)).getAvailableModels();
    }

    @Test
    void testGetModelProviderWithSpecialCharacters() {
      // 准备测试数据
      when(providerManager.getProvider("特殊-provider🌟")).thenReturn(specifiedProvider);

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelProvider provider = modelSelector.getModelProvider("特殊-provider🌟");

      // 验证结果
      assertThat(provider).isSameAs(specifiedProvider);
      verify(providerManager).getProvider("特殊-provider🌟");
    }

    @Test
    void testGetModelProviderWithLongName() {
      // 准备测试数据
      StringBuilder longName = new StringBuilder();
      for (int i = 0; i < 1000; i++) {
        longName.append("long-provider-name");
      }
      String providerName = longName.toString();
      
      when(providerManager.getProvider(providerName)).thenReturn(specifiedProvider);

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelProvider provider = modelSelector.getModelProvider(providerName);

      // 验证结果
      assertThat(provider).isSameAs(specifiedProvider);
      verify(providerManager).getProvider(providerName);
    }

    @Test
    void testGetActualModelNameWithSpecialCharacters() {
      // 准备测试数据
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("特殊-model🌟");
      modelInfo.setAvailable(true);
      
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      String modelName = modelSelector.getActualModelName(specifiedProvider, "特殊-model🌟");

      // 验证结果
      assertThat(modelName).isEqualTo("特殊-model🌟");
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testGetActualModelNameWithLongName() {
      // 准备测试数据
      StringBuilder longName = new StringBuilder();
      for (int i = 0; i < 1000; i++) {
        longName.append("long-model-name");
      }
      String modelName = longName.toString();
      
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName(modelName);
      modelInfo.setAvailable(true);
      
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      String resultModelName = modelSelector.getActualModelName(specifiedProvider, modelName);

      // 验证结果
      assertThat(resultModelName).isEqualTo(modelName);
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testGetActualModelNameWithUnicode() {
      // 准备测试数据
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("Unicode模型测试");
      modelInfo.setAvailable(true);
      
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      String modelName = modelSelector.getActualModelName(specifiedProvider, "Unicode模型测试");

      // 验证结果
      assertThat(modelName).isEqualTo("Unicode模型测试");
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithSpecialCharacters() {
      // 准备测试数据
      when(providerManager.getProvider("特殊-provider🌟")).thenReturn(specifiedProvider);
      
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("特殊-model🌟");
      modelInfo.setAvailable(true);
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(
              1L, "特殊-provider🌟", "特殊-model🌟");

      // 验证结果
      assertThat(selection.provider()).isSameAs(specifiedProvider);
      assertThat(selection.modelName()).isEqualTo("特殊-model🌟");
      verify(providerManager).getProvider("特殊-provider🌟");
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithLongValues() {
      // 准备测试数据
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
      modelInfo.setAvailable(true);
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(
              1L, providerName, modelName);

      // 验证结果
      assertThat(selection.provider()).isSameAs(specifiedProvider);
      assertThat(selection.modelName()).isEqualTo(modelName);
      verify(providerManager).getProvider(providerName);
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithUnicode() {
      // 准备测试数据
      when(providerManager.getProvider("Unicode提供者测试")).thenReturn(specifiedProvider);
      
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("Unicode模型测试");
      modelInfo.setAvailable(true);
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(
              1L, "Unicode提供者测试", "Unicode模型测试");

      // 验证结果
      assertThat(selection.provider()).isSameAs(specifiedProvider);
      assertThat(selection.modelName()).isEqualTo("Unicode模型测试");
      verify(providerManager).getProvider("Unicode提供者测试");
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testGetModelProviderWithWhitespaceName() {
      // 准备测试数据
      when(providerManager.getProvider("  provider  ")).thenReturn(specifiedProvider);

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelProvider provider = modelSelector.getModelProvider("  provider  ");

      // 验证结果
      assertThat(provider).isSameAs(specifiedProvider);
      verify(providerManager).getProvider("  provider  ");
    }

    @Test
    void testGetActualModelNameWithWhitespace() {
      // 准备测试数据
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("  model  ");
      modelInfo.setAvailable(true);
      
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      String modelName = modelSelector.getActualModelName(specifiedProvider, "  model  ");

      // 验证结果
      assertThat(modelName).isEqualTo("  model  ");
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithNullProviderAndValidModel() {
      // 准备测试数据
      when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
      
      ModelInfo modelInfo1 = new ModelInfo();
      modelInfo1.setName("default-model");
      modelInfo1.setAvailable(true);
      
      ModelInfo modelInfo2 = new ModelInfo();
      modelInfo2.setName("valid-model");
      modelInfo2.setAvailable(true);
      
      when(defaultProvider.getAvailableModels()).thenReturn(List.of(modelInfo1, modelInfo2));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(1L, null, "valid-model");

      // 验证结果
      assertThat(selection.provider()).isSameAs(defaultProvider);
      assertThat(selection.modelName()).isEqualTo("valid-model");
      verify(providerManager).getDefaultProvider();
      verify(defaultProvider, times(2)).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithValidProviderAndNullModel() {
      // 准备测试数据
      when(providerManager.getProvider("valid-provider")).thenReturn(specifiedProvider);
      
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("first-model");
      modelInfo.setAvailable(true);
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(1L, "valid-provider", null);

      // 验证结果
      assertThat(selection.provider()).isSameAs(specifiedProvider);
      assertThat(selection.modelName()).isEqualTo("first-model");
      verify(providerManager).getProvider("valid-provider");
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithExceptionInProviderManager() {
      // 准备测试数据
      when(providerManager.getProvider("exception-provider")).thenThrow(new RuntimeException("Provider manager error"));
      when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
      
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("default-model");
      modelInfo.setAvailable(true);
      when(defaultProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试并验证异常
      assertThatThrownBy(() -> modelSelector.selectModelForUser(1L, "exception-provider", null))
              .isInstanceOf(RuntimeException.class)
              .hasMessage("Provider manager error");
              
      verify(providerManager).getProvider("exception-provider");
      verify(providerManager, never()).getDefaultProvider();
    }

    @Test
    void testGetActualModelNameWithExceptionInProvider() {
      // 准备测试数据
      when(specifiedProvider.getAvailableModels()).thenThrow(new RuntimeException("Provider error"));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试并验证异常
      assertThatThrownBy(() -> modelSelector.getActualModelName(specifiedProvider, "any-model"))
              .isInstanceOf(RuntimeException.class)
              .hasMessage("Provider error");
              
      verify(specifiedProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithZeroUserId() {
      // 准备测试数据
      when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
      
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("default-model");
      modelInfo.setAvailable(true);
      when(defaultProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(0L, null, null);

      // 验证结果
      assertThat(selection.provider()).isSameAs(defaultProvider);
      assertThat(selection.modelName()).isEqualTo("default-model");
      verify(providerManager).getDefaultProvider();
      verify(defaultProvider).getAvailableModels();
    }

    @Test
    void testSelectModelForUserWithNegativeUserId() {
      // 准备测试数据
      when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);
      
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("default-model");
      modelInfo.setAvailable(true);
      when(defaultProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelSelector.ModelSelection selection = modelSelector.selectModelForUser(-1L, null, null);

      // 验证结果
      assertThat(selection.provider()).isSameAs(defaultProvider);
      assertThat(selection.modelName()).isEqualTo("default-model");
      verify(providerManager).getDefaultProvider();
      verify(defaultProvider).getAvailableModels();
    }

    @Test
    void testGetModelProviderWithEmptyStringAfterTrim() {
      // 准备测试数据
      when(providerManager.getDefaultProvider()).thenReturn(defaultProvider);

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      ModelProvider provider = modelSelector.getModelProvider("   ");

      // 验证结果
      assertThat(provider).isSameAs(defaultProvider);
      verify(providerManager).getDefaultProvider();
    }

    @Test
    void testGetActualModelNameWithEmptyStringAfterTrim() {
      // 准备测试数据
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setName("default-model");
      modelInfo.setAvailable(true);
      
      when(specifiedProvider.getAvailableModels()).thenReturn(List.of(modelInfo));

      ModelSelector modelSelector = new DefaultModelSelector(providerManager, modelManagementService);

      // 执行测试
      String modelName = modelSelector.getActualModelName(specifiedProvider, "   ");

      // 验证结果
      assertThat(modelName).isEqualTo("default-model");
      verify(specifiedProvider).getAvailableModels();
    }
}