package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Optional<Device> findByDeviceUuid(String deviceUuid);

    // 新增：心跳鉴权与离线扫描需要
    Optional<Device> findByDeviceUuidAndSecretKey(String deviceUuid, String secretKey);


    List<Device> findAllByLastHeartbeatBefore(Instant threshold);
}
