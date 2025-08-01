package com.example.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyBatisConfigTest {

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private MyBatisConfig myBatisConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(myBatisConfig, "dataSource", dataSource);
    }

    @Test
    void testSqlSessionFactory_Success() throws Exception {
        // When
        SqlSessionFactory result = myBatisConfig.sqlSessionFactory();

        // Then
        assertNotNull(result);
        assertNotNull(result.getConfiguration());
        assertTrue(result.getConfiguration().isMapUnderscoreToCamelCase());
        assertTrue(result.getConfiguration().isCacheEnabled());
        assertTrue(result.getConfiguration().isLazyLoadingEnabled());
        // 验证类型别名包配置
        assertNotNull(result.getConfiguration().getTypeAliasRegistry());
    }

    @Test
    void testSqlSessionTemplate_Success() throws Exception {
        // Given
        SqlSessionFactory sqlSessionFactory = myBatisConfig.sqlSessionFactory();

        // When
        SqlSessionTemplate result = myBatisConfig.sqlSessionTemplate(sqlSessionFactory);

        // Then
        assertNotNull(result);
        assertEquals(sqlSessionFactory, result.getSqlSessionFactory());
    }

    @Test
    void testSqlSessionTemplate_WithNullFactory() {
        // Given
        SqlSessionFactory sqlSessionFactory = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            myBatisConfig.sqlSessionTemplate(sqlSessionFactory);
        });
    }
}