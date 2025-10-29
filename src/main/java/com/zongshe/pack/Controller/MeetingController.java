package com.zongshe.pack.Controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会议相关接口", description = "会议的创建、查询、更新、删除等操作")
@CrossOrigin("http://localhost:8089")
@RestController
@RequestMapping("/api/meeting")
public class MeetingController {
}
