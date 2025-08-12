package com.example.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP客户端配置类
 *
 * @author xupeng
 */
@Configuration
public class HttpClientConfig {

  /**
   * 创建并配置HTTP客户端
   *
   * @return 配置好的CloseableHttpClient实例
   */
  @Bean
  public CloseableHttpClient httpClient() {
    // 配置请求参数
    RequestConfig requestConfig =
        RequestConfig.custom()
            // 连接超时10秒
            .setConnectTimeout(10000)
            // 读取超时5分钟，支持长时间流式响应
            .setSocketTimeout(300000)
            // 从连接池获取连接超时5秒
            .setConnectionRequestTimeout(5000)
            .build();

    // 构建并返回HTTP客户端
    return HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        // 最大连接数
        .setMaxConnTotal(100)
        // 每个路由最大连接数
        .setMaxConnPerRoute(20)
        .build();
  }
}
