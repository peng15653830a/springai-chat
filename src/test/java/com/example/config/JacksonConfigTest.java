package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JacksonConfig配置测试
 *
 * @author xupeng
 */
@SpringBootTest
class JacksonConfigTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldConfigureObjectMapper() {
    // Then
    assertNotNull(objectMapper);
  }

  @Test
  void shouldUseSnakeCaseNaming() {
    // Then
    assertEquals(PropertyNamingStrategies.SNAKE_CASE, 
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
    assertTrue(json.contains("first_name"));
    assertTrue(json.contains("last_name"));
    assertFalse(json.contains("firstName"));
    assertFalse(json.contains("lastName"));
  }

  @Test
  void shouldDeserializeSimpleObject() throws Exception {
    // Given
    String json = "{\"first_name\":\"John\",\"last_name\":\"Doe\"}";

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
    assertFalse(json.contains("first_name"));
    assertTrue(json.contains("last_name"));
  }

  @Test
  void shouldCreateJacksonConfig() {
    // Given
    JacksonConfig config = new JacksonConfig();

    // When
    ObjectMapper mapper = config.objectMapper();

    // Then
    assertNotNull(mapper);
    assertEquals(PropertyNamingStrategies.SNAKE_CASE, 
                 mapper.getPropertyNamingStrategy());
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