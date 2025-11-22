package com.zongshe.pack.Controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zongshe.pack.DTO.DeviceHeartbeatRequest;
import com.zongshe.pack.DTO.DeviceHeartbeatResponse;
import com.zongshe.pack.DTO.DeviceRegisterRequest;
import com.zongshe.pack.DTO.DeviceRegisterResponse;
import com.zongshe.pack.Service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;


@Tag(name = "会议室传感器设备相关接口", description = "获取设备在线状态，获取传感器数据，触发设备上报等操作")
@RestController
@RequiredArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeviceController {

    private final DeviceService deviceService;
    @Operation(summary = "设备注册接口", description = "设备通过唯一标识注册到系统，返回注册结果和分配的访问令牌")
    // 表单提交：deviceUuid, roomId, name
    @PostMapping(
            value = "/devices/register",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceRegisterResponse> registerByForm(@Valid DeviceRegisterRequest request) {
        DeviceRegisterResponse resp = deviceService.register(request);
        return ResponseEntity.ok(resp);
    }
    @Operation(summary = "设备注册接口(json)", description = "设备通过唯一标识注册到系统，返回注册结果和分配的访问令牌")
    // 可选：支持 JSON 提交同样的结构
    @PostMapping(
            value = "/devices/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceRegisterResponse> registerByJson(@Valid @RequestBody DeviceRegisterRequest request) {
        DeviceRegisterResponse resp = deviceService.register(request);
        return ResponseEntity.ok(resp);
    }
    @Operation(summary = "设备心跳接口", description = "设备定期发送心跳以更新状态，返回最新的配置信息")
    @PostMapping(
            value = "/devices/heartbeat",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceHeartbeatResponse> heartbeatByForm(@Valid DeviceHeartbeatRequest request) {
        return ResponseEntity.ok(deviceService.heartbeat(request));
    }
    @Operation(summary = "设备心跳接口(json)", description = "设备定期发送心跳以更新状态，返回最新的配置信息")
    @PostMapping(
            value = "/devices/heartbeat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceHeartbeatResponse> heartbeatByJson(@Valid @RequestBody DeviceHeartbeatRequest request) {
        return ResponseEntity.ok(deviceService.heartbeat(request));
    }

    // ---------------- 新增：触发设备上报与获取最新传感器数据 ----------------


    @Operation(summary = "请求设备上报传感器数据(前端使用)", description = "向指定设备发送请求，触发其上报最新的传感器数据")
    @CrossOrigin(origins = "http://localhost:8089")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "请求体示例: 使用 deviceUuid 指定目标设备",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "requestSensorExample",
                            value = "{\"deviceUuid\":\"device-123\"}"
                    ),
                    schema = @Schema(type = "object", example = "{\"deviceUuid\":\"device-123\"}")
            )
    )
    @PostMapping(value = "/api/devices/request-sensor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> requestSensor(@RequestBody Map<String, String> requestBody) {
        String deviceUuid = requestBody.get("deviceUuid");
        if (deviceUuid == null || deviceUuid.isEmpty()) {
            return ResponseEntity.badRequest().body("deviceUuid is required");
        }

        boolean ok = deviceService.requestSensorOnce(deviceUuid);
        if (ok) {
            return ResponseEntity.ok().body("request_sent");
        } else {
            return ResponseEntity.status(503).body("device_offline_or_unavailable");
        }
    }

    @Operation(summary = "获取设备最新传感器数据(前端使用)", description = "获取指定设备最近一次上报的传感器数据,必须保证先运行一次request-sensor")
    @CrossOrigin(origins = "http://localhost:8089")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "请求体示例: 指定 deviceUuid 查询最新传感器数据",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "getLatestSensorExample",
                            value = "{\"deviceUuid\":\"device-123\"}"
                    ),
                    schema = @Schema(type = "object", example = "{\"deviceUuid\":\"device-123\"}")
            )
    )
    @PostMapping(value = "/api/devices/sensor-latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLatestSensor(@RequestBody Map<String, String> requestBody) {
        String deviceUuid = requestBody.get("deviceUuid");
        if (deviceUuid == null || deviceUuid.isEmpty()) {
            return ResponseEntity.badRequest().body("deviceUuid is required");
        }

        Object data = deviceService.getLatestSensorData(deviceUuid);
        if (data == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "获取设备在线状态", description = "通过设备 UUID 查询设备当前的在线状态")
    @CrossOrigin(origins = "http://localhost:8089")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "请求体示例: 查询设备在线状态",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "getDeviceStatusExample",
                            value = "{\"deviceUuid\":\"device-123\"}"
                    ),
                    schema = @Schema(type = "object", example = "{\"deviceUuid\":\"device-123\"}")
            )
    )
    @PostMapping(value = "/api/devices/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDeviceStatus(@RequestBody Map<String, String> requestBody) {
        String deviceUuid = requestBody.get("deviceUuid");
        if (deviceUuid == null || deviceUuid.isEmpty()) {
            return ResponseEntity.badRequest().body("deviceUuid is required");
        }

        boolean isOnline = deviceService.isDeviceOnline(deviceUuid);
        Map<String, Object> response = new HashMap<>();
        response.put("deviceUuid", deviceUuid);
        response.put("online", isOnline);

        return ResponseEntity.ok(response);
    }

}
