package com.zongshe.pack.Websocket;

import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import com.zongshe.pack.Service.DeviceService; // 使用 DeviceService 管理会话和心跳

@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private DeviceService deviceService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 等待设备发送认证消息（如 {"type":"auth","deviceUuid":"...","secretKey":"..."}）
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> m = mapper.readValue(message.getPayload(), Map.class);
        String type = (String) m.get("type");
        if ("auth".equals(type)) {
            String deviceUuid = (String) m.get("deviceUuid");
            String secretKey = (String) m.get("secretKey");
            deviceService.registerSession(deviceUuid, secretKey, session);
            return;
        }
        if ("sensorData".equals(type)) {
            // 设备上报数据，存储并可由 REST 接口或消息系统转发给前端
            String deviceUuid = (String) m.get("deviceUuid");
            Object payload = m.get("payload"); // 即传感器 JSON
            deviceService.forwardSensorDataToFrontend(deviceUuid, payload);
            return;
        }
        if ("heartbeat".equals(type)) {
            String deviceUuid = (String) m.get("deviceUuid");
            deviceService.updateHeartbeat(deviceUuid);
            return;
        }
        // 其它消息处理...
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        deviceService.unregisterSession(session);
    }
}
