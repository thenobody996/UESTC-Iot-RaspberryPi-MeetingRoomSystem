package com.zongshe.pack.Websocket;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.zongshe.pack.Repository.DeviceRepository;
import com.zongshe.pack.Entity.Device;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

@Service
public class DeviceSessionService {

    private final ConcurrentHashMap<String, WebSocketSession> sessionsByDevice = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // 用于推送给前端 /topic/...

    public void registerSession(String deviceUuid, String secretKey, WebSocketSession session) throws Exception {
        // 简单鉴权：查库确认 secretKey（生产请更严谨）
        Device d = deviceRepository.findByDeviceUuidAndSecretKey(deviceUuid, secretKey).orElse(null);
        if (d == null) {
            session.close();
            return;
        }
        sessionsByDevice.put(deviceUuid, session);
        lastHeartbeat.put(deviceUuid, Instant.now());
    }

    public void unregisterSession(WebSocketSession session) {
        sessionsByDevice.entrySet().removeIf(e -> e.getValue().equals(session));
    }

    public void updateHeartbeat(String deviceUuid) {
        lastHeartbeat.put(deviceUuid, Instant.now());
        // 可同时更新 DB 的 lastHeartbeat
    }

    // 服务端向设备下发“请上报传感器数据”请求
    public boolean requestSensorOnce(String deviceUuid) {
        WebSocketSession s = sessionsByDevice.get(deviceUuid);
        if (s == null || !s.isOpen()) return false;
        try {
            String req = "{\"type\":\"requestSensor\"}";
            s.sendMessage(new TextMessage(req));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // 设备上报后调用，将数据推到前端订阅 topic
    public void forwardSensorDataToFrontend(String deviceUuid, Object payload) {
        String topic = "/topic/device/" + deviceUuid + "/sensor";
        messagingTemplate.convertAndSend(topic, payload);
    }

    // 可提供检查在线方法
    public boolean isDeviceOnline(String deviceUuid) {
        WebSocketSession s = sessionsByDevice.get(deviceUuid);
        return s != null && s.isOpen();
    }
}
