package com.zongshe.pack.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import com.zongshe.pack.Websocket.DeviceWebSocketHandler;

@Configuration
@EnableWebSocket
@EnableScheduling
public class DeviceWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private DeviceWebSocketHandler deviceWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(deviceWebSocketHandler, "/ws/device")
                .setAllowedOrigins("*"); // 生产环境限定 origin
    }
}
