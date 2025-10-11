package com.zongshe.pack.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 1. 保留从配置文件读取允许源的逻辑
    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;
    /**
     * 跨域配置过滤器
     *
     * @return CorsFilter
     */
    @Bean
    @ConditionalOnProperty(name = "cors.allowed-origins", matchIfMissing = false)
    public CorsFilter corsFilter() {
        // 创建CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 创建CORS具体配置
        CorsConfiguration config = new CorsConfiguration();

        // 允许携带凭证（如cookies）
        config.setAllowCredentials(true);

        // 设置允许的源，从配置文件读取，支持多个（用逗号分隔）
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // 允许所有的请求头
        config.addAllowedHeader("*");

        // 允许所有的请求方法
        config.addAllowedMethod("*");

        // 设置预检请求的有效期
        config.setMaxAge(3600L);

        // 为所有路径 (/**) 注册这个CORS配置
        source.registerCorsConfiguration("/**", config);

        // 返回最终的CorsFilter实例
        return new CorsFilter(source);
    }
}
