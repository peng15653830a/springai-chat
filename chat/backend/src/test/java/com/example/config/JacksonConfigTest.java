package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JacksonConfig配置测试 - 单元测试
 *
 * @author xupeng
 */
@ActiveProfiles("test")
class JacksonConfigTest {

  private JacksonConfig jacksonConfig;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    jacksonConfig = new JacksonConfig();
    objectMapper = jacksonConfig.objectMapper();
  }

  @Test
  void shouldConfigureObjectMapper() {
    // Then
    assertNotNull(objectMapper);
    assertNotNull(objectMapper.getPropertyNamingStrategy());
  }

  @Test
  void shouldUseLowerCamelCaseNaming() {
    // Then
    assertEquals(PropertyNamingStrategies.LOWER_CAMEL_CASE, 
                 objectMapper.getPropertyNamingStrategy());
  }

  @Test
  void shouldSerializeSimpleObject() throws Exception {
    // Given
    TestObject obj = new TestObject();
    obj.setFirstName("John");
    obj.setLastName("Doe");

    // When
    String json = objectMapper.writeValueAsString(obj);

    // Then
    assertTrue(json.contains("firstName"));
    assertTrue(json.contains("lastName"));
    assertFalse(json.contains("first_name"));
    assertFalse(json.contains("last_name"));
  }

  @Test
  void shouldDeserializeSimpleObject() throws Exception {
    // Given
    String json = "{\"firstName\":\"John\",\"lastName\":\"Doe\"}";

    // When
    TestObject obj = objectMapper.readValue(json, TestObject.class);

    // Then
    assertEquals("John", obj.getFirstName());
    assertEquals("Doe", obj.getLastName());
  }

  @Test
  void shouldHandleNullValues() throws Exception {
    // Given
    TestObject obj = new TestObject();
    obj.setFirstName(null);
    obj.setLastName("Doe");

    // When
    String json = objectMapper.writeValueAsString(obj);

    // Then
    // Jackson会序列化null值
    assertTrue(json.contains("firstName"));
    assertTrue(json.contains("lastName"));
    assertTrue(json.contains("Doe"));
    
    // 验证JSON格式正确
    assertTrue(json.startsWith("{"));
    assertTrue(json.endsWith("}"));
  }

  @Test
  void shouldCreateJacksonConfig() {
    // Given & When
    JacksonConfig config = new JacksonConfig();
    ObjectMapper mapper = config.objectMapper();

    // Then
    assertNotNull(config);
    assertNotNull(mapper);
    assertEquals(PropertyNamingStrategies.LOWER_CAMEL_CASE, 
                 mapper.getPropertyNamingStrategy());
  }

  @Test
  void shouldConfigureJsr310Module() {
    // Then
    assertTrue(objectMapper.getRegisteredModuleIds().size() > 0);
  }

  @Test
  void shouldDisableWriteDatesAsTimestamps() {
    // Then
    assertFalse(objectMapper.isEnabled(
        com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
  }

  @Test
  void shouldIgnoreUnknownProperties() {
    // Then
    assertFalse(objectMapper.isEnabled(
        com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
  }

  // 测试用的简单对象
  public static class TestObject {
    private String firstName;
    private String lastName;

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }
  }
}