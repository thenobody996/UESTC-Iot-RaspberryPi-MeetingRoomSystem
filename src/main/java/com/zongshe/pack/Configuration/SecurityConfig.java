// java
package com.zongshe.pack.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // 使用已定义的 CorsConfigurationSource
                .csrf(csrf -> csrf.disable())    // 根据需要禁用 CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // 放行所有请求
                        .anyRequest().authenticated()                          // 其余按需调整
                );

        return http.build();
    }
}
