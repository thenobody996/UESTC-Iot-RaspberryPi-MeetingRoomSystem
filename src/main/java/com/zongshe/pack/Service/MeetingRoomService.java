package com.zongshe.pack.Service;

import com.zongshe.pack.Common.MeetingRoomRequest;
import com.zongshe.pack.Entity.MeetingRoom;
import com.zongshe.pack.Repository.MeetingRoomRepository;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MeetingRoomService {

    @Autowired
    public MeetingRoomRepository meetingRoomRepository;

    @Autowired
    public UserService userService;

    public List<MeetingRoom> getAllMeetingRooms(Integer page, Integer pageSize) {
        return meetingRoomRepository.findByIsDeletedIsFalse(PageRequest.of(page, pageSize)).getContent();
    }

    public MeetingRoom getMeetingRoomById(Integer id) {
        return meetingRoomRepository.findByIdAndIsDeletedFalse(id);
    }

    public List<MeetingRoom> searchMeetingRoomsByName(String name, Integer page, Integer pageSize) {
        return meetingRoomRepository.findByNameContainingAndIsDeletedFalse(name, PageRequest.of(page, pageSize)).getContent();
    }

    public Long countRooms(){
        return meetingRoomRepository.countByIsDeletedFalse();
    }

    //创建/更新会议室信息
    public MeetingRoom setMeetingRoom(MeetingRoom meetingRoom, MeetingRoomRequest meetingRoomRequest) throws Exception {
        if (meetingRoom == null) {
            throw new Exception("会议室不存在或已删除");
        }
        meetingRoom.setName(meetingRoomRequest.getName());
        meetingRoom.setManager(userService.getUserById(meetingRoomRequest.getManager_id()));
        meetingRoom.setVolume(meetingRoomRequest.getVolume());
        meetingRoom.setDescription(meetingRoomRequest.getDescription());
        meetingRoom.setLocateURL(meetingRoomRequest.getLocateURL());
        meetingRoom.setUpdateAt(LocalDateTime.now());
        meetingRoom.setIsDeleted(false);
        return meetingRoomRepository.save(meetingRoom);
    }

    public MeetingRoom DeleteMeetingRoom(MeetingRoom meetingRoom){
        meetingRoom.setIsDeleted(true);
        return meetingRoomRepository.save(meetingRoom);
    }

    //向会议室注册会议信息
}
