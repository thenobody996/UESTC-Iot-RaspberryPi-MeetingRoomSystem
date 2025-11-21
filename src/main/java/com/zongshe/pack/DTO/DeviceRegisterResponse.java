package com.zongshe.pack.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegisterResponse {
    private Integer deviceId;
    private Integer roomId;
    private String secretKey;
    // true 表示注册成功，false 表示注册失败（失败时其他字段为 null）
    private Boolean status;
}
