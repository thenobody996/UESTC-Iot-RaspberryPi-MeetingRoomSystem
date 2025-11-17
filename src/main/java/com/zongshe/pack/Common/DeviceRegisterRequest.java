package com.zongshe.pack.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceRegisterRequest {
    // 设备唯一标识（MAC/UUID）
    @NotBlank
    @Size(max = 64)
    private String deviceUuid;

    // 设备所在会议室
    @NotNull
    private Integer roomId;

    // 设备名称（可选）
    @Size(max = 100)
    private String name;
}
