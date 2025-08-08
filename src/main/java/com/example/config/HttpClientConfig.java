package com.example.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {
    
    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000) // 连接超时10秒
                .setSocketTimeout(300000) // 读取超时5分钟，支持长时间流式响应
                .setConnectionRequestTimeout(5000) // 从连接池获取连接超时5秒
                .build();
        
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnTotal(100) // 最大连接数
                .setMaxConnPerRoute(20) // 每个路由最大连接数
                .build();
    }
}