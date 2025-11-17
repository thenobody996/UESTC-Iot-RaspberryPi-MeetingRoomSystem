package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Optional<Device> findByDeviceUuid(String deviceUuid);

}
