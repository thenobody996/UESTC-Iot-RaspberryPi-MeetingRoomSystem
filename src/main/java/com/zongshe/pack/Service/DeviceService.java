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

import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    @Autowired
    private final DeviceRepository deviceRepository;

    @Autowired
    private final MeetingRoomRepository meetingRoomRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

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

        return new DeviceHeartbeatResponse(
                true,
                device.getId(),
                device.getStatus(),
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                3 // 建议 3 秒一拍，可按需调整
        );
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
