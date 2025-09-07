package com.example.dto.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProviderInfo DTO测试类
 *
 * @author xupeng
 */
class ProviderInfoTest {

    @Test
    void shouldCreateProviderInfo() {
        // When
        ProviderInfo providerInfo = new ProviderInfo();

        // Then
        assertThat(providerInfo).isNotNull();
    }

    @Test
    void shouldSetAndGetId() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        Long id = 1L;

        // When
        providerInfo.setId(id);

        // Then
        assertThat(providerInfo.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetName() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        String name = "DeepSeek";

        // When
        providerInfo.setName(name);

        // Then
        assertThat(providerInfo.getName()).isEqualTo(name);
    }

    @Test
    void shouldSetAndGetDisplayName() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        String displayName = "DeepSeek API";

        // When
        providerInfo.setDisplayName(displayName);

        // Then
        assertThat(providerInfo.getDisplayName()).isEqualTo(displayName);
    }

    @Test
    void shouldSetAndGetAvailable() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        Boolean available = true;

        // When
        providerInfo.setAvailable(available);

        // Then
        assertThat(providerInfo.getAvailable()).isEqualTo(available);
    }

    @Test
    void shouldSetAndGetModels() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        List<ModelInfo> models = Arrays.asList(new ModelInfo(), new ModelInfo());

        // When
        providerInfo.setModels(models);

        // Then
        assertThat(providerInfo.getModels()).isEqualTo(models);
    }

    @Test
    void shouldGetAvailableModelCountWithNullModels() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setModels(null);

        // When
        int count = providerInfo.getAvailableModelCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldGetAvailableModelCountWithEmptyModels() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setModels(Collections.emptyList());

        // When
        int count = providerInfo.getAvailableModelCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldGetAvailableModelCountWithAvailableModels() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        
        ModelInfo model1 = new ModelInfo();
        model1.setAvailable(true);
        
        ModelInfo model2 = new ModelInfo();
        model2.setAvailable(true);
        
        ModelInfo model3 = new ModelInfo();
        model3.setAvailable(false);
        
        providerInfo.setModels(Arrays.asList(model1, model2, model3));

        // When
        int count = providerInfo.getAvailableModelCount();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldGetAvailableModelCountWithAllUnavailableModels() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        
        ModelInfo model1 = new ModelInfo();
        model1.setAvailable(false);
        
        ModelInfo model2 = new ModelInfo();
        model2.setAvailable(false);
        
        providerInfo.setModels(Arrays.asList(model1, model2));

        // When
        int count = providerInfo.getAvailableModelCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldGetAvailableModelCountWithNullAvailableFlags() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        
        ModelInfo model1 = new ModelInfo();
        model1.setAvailable(null);
        
        ModelInfo model2 = new ModelInfo();
        model2.setAvailable(null);
        
        providerInfo.setModels(Arrays.asList(model1, model2));

        // When
        int count = providerInfo.getAvailableModelCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        // Given
        ProviderInfo providerInfo1 = new ProviderInfo();
        providerInfo1.setId(1L);
        providerInfo1.setName("DeepSeek");

        ProviderInfo providerInfo2 = new ProviderInfo();
        providerInfo2.setId(1L);
        providerInfo2.setName("DeepSeek");

        ProviderInfo providerInfo3 = new ProviderInfo();
        providerInfo3.setId(2L);
        providerInfo3.setName("DifferentProvider");

        // Then
        assertThat(providerInfo1).isEqualTo(providerInfo2);
        assertThat(providerInfo1).isNotEqualTo(providerInfo3);
        assertThat(providerInfo1.hashCode()).isEqualTo(providerInfo2.hashCode());
    }

    @Test
    void shouldTestToString() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setId(1L);
        providerInfo.setName("DeepSeek");

        // When
        String toString = providerInfo.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("1");
        assertThat(toString).contains("DeepSeek");
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        ProviderInfo providerInfo = new ProviderInfo();

        // Then
        assertThat(providerInfo.getId()).isNull();
        assertThat(providerInfo.getName()).isNull();
        assertThat(providerInfo.getDisplayName()).isNull();
        assertThat(providerInfo.getAvailable()).isNull();
        assertThat(providerInfo.getModels()).isNull();
    }
}