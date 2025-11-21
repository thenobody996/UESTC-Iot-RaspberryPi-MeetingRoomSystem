package com.zongshe.pack.DTO;

import com.zongshe.pack.Entity.Device;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceHeartbeatResponse {
    // 本次心跳是否被接受（鉴权通过并已更新）
    private boolean ok;
    private Integer deviceId;
    private Device.DeviceStatus deviceStatus;
    private String serverTime; // ISO-8601 字符串
    private int nextHeartbeatIntervalSec; // 建议的下次心跳间隔（秒）
}
