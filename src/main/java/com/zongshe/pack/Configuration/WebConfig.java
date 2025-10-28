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
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    /**
     * 跨域配置过滤器
     *
     * 说明：
     * - 在 `application.properties` 中设置：cors.allowed-origins=http://frontend.example.com,http://*.example.com
     * - 支持精确 origin 或带 `*` 的模式；当包含 `*` 时使用 setAllowedOriginPatterns 来匹配。
     */
    @Bean
    @ConditionalOnProperty(name = "cors.allowed-origins", matchIfMissing = false)
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许携带凭证（如 cookies）
        config.setAllowCredentials(true);

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (!origins.isEmpty()) {
            boolean hasPattern = origins.stream().anyMatch(s -> s.contains("*"));
            if (hasPattern) {
                // 支持通配符模式，Spring 会在响应中回显匹配到的 origin（适合 withCredentials=true 的场景）
                config.setAllowedOriginPatterns(origins);
            } else {
                // 精确 origin 列表
                config.setAllowedOrigins(origins);
            }
        }

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
