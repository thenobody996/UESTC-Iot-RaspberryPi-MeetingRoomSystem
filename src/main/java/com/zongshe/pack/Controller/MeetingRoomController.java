package com.zongshe.pack.Controller;

import com.zongshe.pack.DTO.MeetingRoomRequest;
import com.zongshe.pack.Common.Result;
import com.zongshe.pack.Entity.MeetingRoom;
import com.zongshe.pack.Service.MeetingRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "会议室相关接口", description = "会议室的创建、查询、更新、删除等操作")
@CrossOrigin("http://localhost:8089")
@RestController
@RequestMapping("/api/meetingroom")
public class MeetingRoomController {

    @Autowired
    private MeetingRoomService meetingRoomService;

    /**
     * 获取所有会议室
     * @param page
     * @param size
     * @return
     */
    @Operation(summary = "获取所有会议室", description = "传入分页params{page,size},获取分页的会议室列表")
    @GetMapping("/allrooms")
    public ResponseEntity<Object> GetAllRoom(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size){
        List<MeetingRoom> roomList = meetingRoomService.getAllMeetingRooms(page, size);
        Long total = meetingRoomService.countRooms();

        Map<String, Object> result = new HashMap<>();
        result.put("list",roomList);
        result.put("total", total);
        return ResponseEntity.ok().body(result);
    }

    @Operation(summary = "根据id查询会议室")
    @GetMapping("/{id}")
    public ResponseEntity<MeetingRoom> GetRoomById(@PathVariable Integer id){
        return ResponseEntity.ok().body(meetingRoomService.getMeetingRoomById(id));
    }

    @GetMapping("/search/{name}")
    @Operation(summary = "根据名称模糊查询会议室", description = "传入会议室名称的部分字符串,分页参数{page,size},获取分页的会议室列表")
    public ResponseEntity<Object> GetRoomsByName(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size){
        List<MeetingRoom> roomList = meetingRoomService.searchMeetingRoomsByName(name, page, size);
        Long total = meetingRoomService.countRooms();

        Map<String, Object> result = new HashMap<>();
        result.put("list",roomList);
        result.put("total", total);
        return ResponseEntity.ok().body(result);
    }

    @Operation(summary = "创建新会议室", description = "请求体构造会议室相关信息,创建新会议室")
    @PostMapping("/")
    public ResponseEntity<Result<MeetingRoom>> PostNewRoom(
            @RequestBody MeetingRoomRequest request) throws Exception {
        try {
            MeetingRoom newRoom = new MeetingRoom();
            return ResponseEntity.ok().body(Result.ok(meetingRoomService.setMeetingRoom(newRoom, request),"会议室创建成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail(e.getMessage()));
        }
    }

    @Operation(summary = "更新会议室相关信息", description = "提供要修改的会议室id和修改信息")
    @PutMapping("/update/{roomId}")
    public ResponseEntity<MeetingRoom> PutRoom(
            @PathVariable Integer roomId,
            @RequestBody MeetingRoomRequest request
    ) throws Exception {
        MeetingRoom room = meetingRoomService.getMeetingRoomById(roomId);
        return ResponseEntity.ok().body(meetingRoomService.setMeetingRoom(room, request));
    }

    @Operation(summary = "删除会议室")
    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<Object> DeleteRoom(@PathVariable Integer roomId){
        MeetingRoom room = meetingRoomService.getMeetingRoomById(roomId);
        return ResponseEntity.ok().body(meetingRoomService.DeleteMeetingRoom(room));
    }

    @GetMapping("/deviceuuid/{roomId}")
    @Operation(summary = "获取会议室绑定设备的UUID", description = "根据会议室ID获取其绑定设备的UUID,如果没有绑定设备则返回null")
    public ResponseEntity<String> getDeviceUuid(@PathVariable Integer roomId){
        MeetingRoom room = meetingRoomService.getMeetingRoomById(roomId);
        if(room.getDevice() != null) {
            return ResponseEntity.ok().body(room.getDevice().getDeviceUuid());
        }
        return ResponseEntity.ok().body(null);
    }
}
