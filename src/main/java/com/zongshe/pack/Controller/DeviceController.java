package com.zongshe.pack.Controller;

import com.zongshe.pack.DTO.DeviceHeartbeatRequest;
import com.zongshe.pack.DTO.DeviceHeartbeatResponse;
import com.zongshe.pack.DTO.DeviceRegisterRequest;
import com.zongshe.pack.DTO.DeviceRegisterResponse;
import com.zongshe.pack.Service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    // 表单提交：deviceUuid, roomId, name
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceRegisterResponse> registerByForm(@Valid DeviceRegisterRequest request) {
        DeviceRegisterResponse resp = deviceService.register(request);
        return ResponseEntity.ok(resp);
    }

    // 可选：支持 JSON 提交同样的结构
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceRegisterResponse> registerByJson(@Valid @RequestBody DeviceRegisterRequest request) {
        DeviceRegisterResponse resp = deviceService.register(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping(
            value = "/heartbeat",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceHeartbeatResponse> heartbeatByForm(@Valid DeviceHeartbeatRequest request) {
        return ResponseEntity.ok(deviceService.heartbeat(request));
    }

    @PostMapping(
            value = "/heartbeat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceHeartbeatResponse> heartbeatByJson(@Valid @RequestBody DeviceHeartbeatRequest request) {
        return ResponseEntity.ok(deviceService.heartbeat(request));
    }

    // ---------------- 新增：触发设备上报与获取最新传感器数据 ----------------

    @PostMapping(value = "/{deviceUuid}/request-sensor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> requestSensor(@PathVariable String deviceUuid) {
        boolean ok = deviceService.requestSensorOnce(deviceUuid);
        if (ok) {
            return ResponseEntity.ok().body("request_sent");
        } else {
            return ResponseEntity.status(503).body("device_offline_or_unavailable");
        }
    }

    @GetMapping(value = "/{deviceUuid}/sensor-latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLatestSensor(@PathVariable String deviceUuid) {
        Object data = deviceService.getLatestSensorData(deviceUuid);
        if (data == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(data);
    }
}
