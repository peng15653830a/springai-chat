package com.example.controller;

import com.example.dto.response.ApiResponse;
import com.example.dto.common.ModelInfo;
import com.example.dto.common.ProviderInfo;
import com.example.service.ModelManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelControllerTest {

    @Mock
    private ModelManagementService modelManagementService;

    @InjectMocks
    private ModelController modelController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetAvailableProviders_Success() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setName("testProvider");
        providerInfo.setDisplayName("Test Provider");
        
        List<ProviderInfo> providers = Arrays.asList(providerInfo);
        when(modelManagementService.getAvailableProviders()).thenReturn(providers);

        // When
        ApiResponse<List<ProviderInfo>> response = modelController.getAvailableProviders();

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("testProvider", response.getData().get(0).getName());
        
        verify(modelManagementService).getAvailableProviders();
    }

    @Test
    void testGetAvailableProviders_Exception() {
        // Given
        when(modelManagementService.getAvailableProviders()).thenThrow(new RuntimeException("Test exception"));

        // When
        ApiResponse<List<ProviderInfo>> response = modelController.getAvailableProviders();

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("获取提供者列表失败"));
        
        verify(modelManagementService).getAvailableProviders();
    }

    @Test
    void testGetProviderModels_Success() {
        // Given
        String providerName = "testProvider";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("testModel");
        modelInfo.setDisplayName("Test Model");
        
        List<ModelInfo> models = Arrays.asList(modelInfo);
        when(modelManagementService.getProviderModels(providerName)).thenReturn(models);

        // When
        ApiResponse<List<ModelInfo>> response = modelController.getProviderModels(providerName);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("testModel", response.getData().get(0).getName());
        
        verify(modelManagementService).getProviderModels(providerName);
    }

    @Test
    void testGetProviderModels_Exception() {
        // Given
        String providerName = "testProvider";
        when(modelManagementService.getProviderModels(providerName)).thenThrow(new RuntimeException("Test exception"));

        // When
        ApiResponse<List<ModelInfo>> response = modelController.getProviderModels(providerName);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("获取模型列表失败"));
        
        verify(modelManagementService).getProviderModels(providerName);
    }

    @Test
    void testGetAllAvailableModels_Success() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setName("testProvider");
        providerInfo.setDisplayName("Test Provider");
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName("testModel");
        modelInfo.setDisplayName("Test Model");
        
        providerInfo.setModels(Arrays.asList(modelInfo));
        
        List<ProviderInfo> providersWithModels = Arrays.asList(providerInfo);
        when(modelManagementService.getAllAvailableModels()).thenReturn(providersWithModels);

        // When
        ApiResponse<List<ProviderInfo>> response = modelController.getAllAvailableModels();

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("testProvider", response.getData().get(0).getName());
        assertEquals(1, response.getData().get(0).getModels().size());
        
        verify(modelManagementService).getAllAvailableModels();
    }

    @Test
    void testGetAllAvailableModels_Exception() {
        // Given
        when(modelManagementService.getAllAvailableModels()).thenThrow(new RuntimeException("Test exception"));

        // When
        ApiResponse<List<ProviderInfo>> response = modelController.getAllAvailableModels();

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("获取模型列表失败"));
        
        verify(modelManagementService).getAllAvailableModels();
    }

    @Test
    void testGetModelInfo_Success() {
        // Given
        String providerName = "testProvider";
        String modelName = "testModel";
        
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setName(modelName);
        modelInfo.setDisplayName("Test Model");
        
        when(modelManagementService.getModelInfo(providerName, modelName)).thenReturn(modelInfo);

        // When
        ApiResponse<ModelInfo> response = modelController.getModelInfo(providerName, modelName);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(modelName, response.getData().getName());
        assertEquals("Test Model", response.getData().getDisplayName());
        
        verify(modelManagementService).getModelInfo(providerName, modelName);
    }

    @Test
    void testGetModelInfo_NotFound() {
        // Given
        String providerName = "testProvider";
        String modelName = "nonExistentModel";
        
        when(modelManagementService.getModelInfo(providerName, modelName)).thenReturn(null);

        // When
        ApiResponse<ModelInfo> response = modelController.getModelInfo(providerName, modelName);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("模型不存在", response.getMessage());
        
        verify(modelManagementService).getModelInfo(providerName, modelName);
    }

    @Test
    void testGetModelInfo_Exception() {
        // Given
        String providerName = "testProvider";
        String modelName = "testModel";
        
        when(modelManagementService.getModelInfo(providerName, modelName)).thenThrow(new RuntimeException("Test exception"));

        // When
        ApiResponse<ModelInfo> response = modelController.getModelInfo(providerName, modelName);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("获取模型信息失败"));
        
        verify(modelManagementService).getModelInfo(providerName, modelName);
    }

    @Test
    void testCheckModelAvailability_Success() {
        // Given
        String providerName = "testProvider";
        String modelName = "testModel";
        
        when(modelManagementService.isModelAvailable(providerName, modelName)).thenReturn(true);

        // When
        ApiResponse<Boolean> response = modelController.checkModelAvailability(providerName, modelName);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getData());
        
        verify(modelManagementService).isModelAvailable(providerName, modelName);
    }

    @Test
    void testCheckModelAvailability_Exception() {
        // Given
        String providerName = "testProvider";
        String modelName = "testModel";
        
        when(modelManagementService.isModelAvailable(providerName, modelName)).thenThrow(new RuntimeException("Test exception"));

        // When
        ApiResponse<Boolean> response = modelController.checkModelAvailability(providerName, modelName);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("检查模型可用性失败"));
        
        verify(modelManagementService).isModelAvailable(providerName, modelName);
    }
}