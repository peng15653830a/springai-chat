package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 长城大模型特有配置属性
 * 
 * @author xupeng
 */
@Data
@Component
@ConfigurationProperties(prefix = "greatwall")
public class GreatWallProperties {

    /**
     * SSL配置
     */
    private Ssl ssl = new Ssl();

    @Data
    public static class Ssl {
        /**
         * 是否跳过SSL证书验证
         */
        private boolean skipVerification = true;
    }
}