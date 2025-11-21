package com.zongshe.pack.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceHeartbeatRequest {
    @NotBlank
    private String deviceUuid;

    @NotBlank
    private String secretKey;
}
