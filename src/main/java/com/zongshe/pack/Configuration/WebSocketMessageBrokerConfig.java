package com.zongshe.pack.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册前端/设备连接的 STOMP 端点（支持 SockJS 回退）
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用内存中的简单 broker（用于向前端推送），并设置前端订阅前缀
        registry.enableSimpleBroker("/topic", "/queue");
        // 后端 @MessageMapping 接收前端消息时使用的前缀
        registry.setApplicationDestinationPrefixes("/app");
    }
}

