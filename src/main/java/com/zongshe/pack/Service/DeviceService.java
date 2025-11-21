package com.zongshe.pack.Service;

import com.zongshe.pack.DTO.DeviceHeartbeatRequest;
import com.zongshe.pack.DTO.DeviceHeartbeatResponse;
import com.zongshe.pack.DTO.DeviceRegisterRequest;
import com.zongshe.pack.DTO.DeviceRegisterResponse;
import com.zongshe.pack.Entity.Device;
import com.zongshe.pack.Entity.MeetingRoom;
import com.zongshe.pack.Repository.DeviceRepository;
import com.zongshe.pack.Repository.MeetingRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DeviceService {

    @Autowired
    private final DeviceRepository deviceRepository;

    @Autowired
    private final MeetingRoomRepository meetingRoomRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    // WebSocket 会话和心跳管理（内存缓存）
    private final ConcurrentHashMap<String, WebSocketSession> sessionsByDevice = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> latestSensorData = new ConcurrentHashMap<>();

    // SimpMessagingTemplate 可选注入（如果启用了 STOMP broker）
    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public DeviceRegisterResponse register(DeviceRegisterRequest request) {
        // 校验会议室是否存在
        Optional<MeetingRoom> roomOpt = meetingRoomRepository.findById(request.getRoomId());
        if (roomOpt.isEmpty()) {
            return new DeviceRegisterResponse(null, null, null, false);
        }

        // 校验 deviceUuid 是否已注册
        if (deviceRepository.findByDeviceUuid(request.getDeviceUuid()).isPresent()) {
            return new DeviceRegisterResponse(null, null, null, false);
        }

        // 生成 secretKey
        String secretKey = generateSecretKey();

        // 创建设备
        Device device = new Device();
        device.setDeviceUuid(request.getDeviceUuid());
        device.setName(request.getName());
        device.setSecretKey(secretKey);
        device.setMeetingRoom(roomOpt.get());
        // 状态默认由 @PrePersist 设为 offline

        Device saved = deviceRepository.save(device);

        return new DeviceRegisterResponse(
                saved.getId(),
                roomOpt.get().getId(),
                secretKey,
                true
        );
    }

    private String generateSecretKey() {
        byte[] bytes = new byte[32]; // 256-bit
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Transactional
    public DeviceHeartbeatResponse heartbeat(DeviceHeartbeatRequest request) {
        Optional<Device> opt = deviceRepository.findByDeviceUuidAndSecretKey(
                request.getDeviceUuid(), request.getSecretKey()
        );
        if (opt.isEmpty()) {
            return new DeviceHeartbeatResponse(
                    false, null, null, DateTimeFormatter.ISO_INSTANT.format(Instant.now()), 3
            );
        }

        Device device = opt.get();
        device.setLastHeartbeat(Instant.now());
        device.setStatus(Device.DeviceStatus.online);
        deviceRepository.save(device);

        // 更新内存心跳
        lastHeartbeat.put(device.getDeviceUuid(), device.getLastHeartbeat());

        return new DeviceHeartbeatResponse(
                true,
                device.getId(),
                device.getStatus(),
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                3 // 建议 3 秒一拍，可按需调整
        );
    }

    // ---------------- WebSocket 会话管理方法 ----------------

    public void registerSession(String deviceUuid, String secretKey, WebSocketSession session) {
        // 简单鉴权：校验 DB 中 deviceUuid 和 secretKey
        Optional<Device> dOpt = deviceRepository.findByDeviceUuidAndSecretKey(deviceUuid, secretKey);
        if (dOpt.isEmpty()) {
            try {
                session.close();
            } catch (Exception ignored) {}
            return;
        }
        sessionsByDevice.put(deviceUuid, session);
        lastHeartbeat.put(deviceUuid, Instant.now());

        // 更新 DB 状态
        Device d = dOpt.get();
        d.setLastHeartbeat(Instant.now());
        d.setStatus(Device.DeviceStatus.online);
        deviceRepository.save(d);

        System.out.println("Device WebSocket registered: " + deviceUuid);
    }

    public void unregisterSession(WebSocketSession session) {
        sessionsByDevice.entrySet().removeIf(e -> e.getValue().equals(session));
    }

    public void updateHeartbeat(String deviceUuid) {
        lastHeartbeat.put(deviceUuid, Instant.now());
        Optional<Device> opt = deviceRepository.findByDeviceUuid(deviceUuid);
        if (opt.isPresent()) {
            Device device = opt.get();
            device.setLastHeartbeat(Instant.now());
            device.setStatus(Device.DeviceStatus.online);
            deviceRepository.save(device);
        }
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

    // 设备上报后调用，将数据推到前端订阅 topic（如果 STOMP 可用）或记录到内存供 REST 查询
    public void forwardSensorDataToFrontend(String deviceUuid, Object payload) {
        // 保存最近一次数据
        latestSensorData.put(deviceUuid, payload);

        // 1) 如果有 STOMP messagingTemplate，则推送
        if (messagingTemplate != null) {
            String topic = "/topic/device/" + deviceUuid + "/sensor";
            messagingTemplate.convertAndSend(topic, payload);
        }

        // 2) 打印日志以便调试
        System.out.println("Received sensor data from " + deviceUuid + ": " + payload);
    }

    // REST 查询最近一次传感器数据（可在 Controller 中调用）
    public Object getLatestSensorData(String deviceUuid) {
        return latestSensorData.get(deviceUuid);
    }

    // 每 2 秒扫描一次，把超过 5 秒未心跳的设备置为 OFFLINE
    @Transactional
    @Scheduled(fixedDelay = 2000)
    public void markOfflineDevices() {
        System.out.println("Executing markOfflineDevices method..."); // 控制台提示方法执行

        Instant threshold = Instant.now().minusSeconds(5);
        List<Device> stale = deviceRepository.findAllByLastHeartbeatBefore(threshold);
        for (Device d : stale) {
            if (d.getStatus() != Device.DeviceStatus.offline) {
                d.setStatus(Device.DeviceStatus.offline);
                System.out.println("Device " + d.getId() + " set to offline."); // 控制台提示设备离线

            }
        }
        if (!stale.isEmpty()) {
            deviceRepository.saveAll(stale);
        }
    }
}
