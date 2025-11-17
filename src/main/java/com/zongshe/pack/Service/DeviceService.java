package com.zongshe.pack.Service;

import com.zongshe.pack.DTO.DeviceRegisterRequest;
import com.zongshe.pack.DTO.DeviceRegisterResponse;
import com.zongshe.pack.Entity.Device;
import com.zongshe.pack.Entity.MeetingRoom;
import com.zongshe.pack.Repository.DeviceRepository;
import com.zongshe.pack.Repository.MeetingRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
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
}
