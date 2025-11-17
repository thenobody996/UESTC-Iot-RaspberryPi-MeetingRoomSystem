package com.zongshe.pack.Controller;

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
@RequestMapping("/api/devices")
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
}

