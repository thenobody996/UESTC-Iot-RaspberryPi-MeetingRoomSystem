package com.zongshe.pack.Controller;

import com.zongshe.pack.Common.MeetingRequest;
import com.zongshe.pack.Entity.Meeting;
import com.zongshe.pack.Service.MeetingRoomService;
import com.zongshe.pack.Service.MeetingService;
import com.zongshe.pack.Service.UserService;
import com.zongshe.pack.Common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "会议相关接口", description = "会议的创建、查询、更新、删除等操作")
@CrossOrigin("http://localhost:8089")
@RestController
@RequestMapping("/api/meeting")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private UserService userService;

    @Autowired
    private MeetingRoomService meetingRoomService;
    /**
     * 获取所有会议
     * @param page
     * @param size
     * @return
     */
    @Operation(summary = "获取所有会议", description = "传入分页params{page,size},获取分页的会议列表")
    @GetMapping("/allmeetings")
    public ResponseEntity<Object> GetAllMeetings(
            @RequestParam Integer page,
            @RequestParam Integer size) {
        List<Meeting> meetingList = meetingService.getAllMeetings();
        Long total = meetingService.countMeetings();

        Map<String, Object> result = new HashMap<>();
        result.put("list", meetingList);
        result.put("total", total);

        return ResponseEntity.ok().body(result);
    }

    @Operation(summary = "根据id查询会议")
    @GetMapping("/{id}")
    public ResponseEntity<Meeting> GetMeetingById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(meetingService.getMeetingById(id));
    }

    @Operation(summary = "创建新会议",description = "注意使用两次.data进行解引用")
    @PostMapping("/")
    public ResponseEntity<Result<Meeting>> PostNewMeeting(@RequestBody MeetingRequest request) throws Exception {
        try {
            Meeting result = meetingService.addMeeting(
                    request.getTitle(),
                    request.getDescription(),
                    request.getStart_time(),
                    request.getEnd_time(),
                    userService.getUserById(request.getHost_id()),
                    meetingRoomService.getMeetingRoomById(request.getPlace_id()),
                    request.getMembers_id()
            );
            return ResponseEntity.ok().body(Result.ok(result,"会议创建成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail(e.getMessage()));
        }
    }

    @Operation(summary = "更新会议信息(不含成员信息)",description = "根据会议ID更新会议信息，注意不包含成员信息的更新,使用两次.data解引用")
    @PutMapping("/{id}")
    public ResponseEntity<Result<Meeting>> UpdateMeeting(@PathVariable Integer id, @RequestBody MeetingRequest request) {
        try {
            if(meetingService.getMeetingById(id) == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail("会议不存在或已删除"));
            }
            Meeting result = meetingService.updateMeeting(
                    id,
                    request.getTitle(),
                    request.getDescription(),
                    request.getStart_time(),
                    request.getEnd_time()
            );
            return ResponseEntity.ok().body(Result.ok(result,"会议更新成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail(e.getMessage()));
        }
    }

    @Operation(summary = "更新会议成员信息",description = "根据会议ID更新会议成员信息,使用两次.data解引用")
    @PatchMapping("/updatemembers/{id}")
    public ResponseEntity<Result<Meeting>> UpdateMembers(
            @PathVariable Integer id,
            @RequestBody List<Integer> members) {
        try {
            if(meetingService.getMeetingById(id) == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail("会议不存在或已删除"));
            }
            Meeting result = meetingService.updateMembers(id, members);
            return ResponseEntity.ok().body(Result.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail(e.getMessage()));
        }
    }

    @Operation(summary = "向会议添加成员",description = "根据会议ID添加会议成员,使用两次.data解引用")
    @PostMapping("/addmember/{id}")
    public ResponseEntity<Result<Meeting>> AddMembers(
            @PathVariable Integer id,
            @RequestParam Integer member) {
        try {
            if(meetingService.getMeetingById(id) == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail("会议不存在或已删除"));
            }
            Meeting result = meetingService.addMember(id, member);
            return ResponseEntity.ok().body(Result.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail(e.getMessage()));
        }
    }

    @Operation(summary = "向会议添加成员",description = "根据会议ID添加会议成员,使用两次.data解引用")
    @PostMapping("/removemember/{id}")
    public ResponseEntity<Result<Meeting>> RemoveMembers(
            @PathVariable Integer id,
            @RequestParam Integer member) {
        try {
            if(meetingService.getMeetingById(id) == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail("会议不存在或已删除"));
            }
            Meeting result = meetingService.removeMember(id, member);
            return ResponseEntity.ok().body(Result.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail(e.getMessage()));
        }
    }

    @Operation(summary = "删除会议",description = "根据会议ID删除会议,使用两次.data解引用")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Result<Meeting>> DeleteMeeting(@PathVariable Integer id) {
        try {
            Meeting meeting = meetingService.getMeetingById(id);
            if(meeting == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail("会议不存在或已删除"));
            }
            Meeting result = meetingService.deleteMeeting(id);
            return ResponseEntity.ok().body(Result.ok(result,"会议删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail(e.getMessage()));
        }
    }
}
