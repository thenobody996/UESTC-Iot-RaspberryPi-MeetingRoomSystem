package com.zongshe.pack.Service;

import com.zongshe.pack.Entity.Meeting;
import com.zongshe.pack.Entity.MeetingRoom;
import com.zongshe.pack.Entity.User;
import com.zongshe.pack.Repository.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;


    @Autowired UserService userService;

    public Meeting addMeeting(String title, String description,
                              LocalDateTime start, LocalDateTime end,
                              User hoster, MeetingRoom place,
                              List<Integer> members) throws Exception {
        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setStartTime(start);
        meeting.setEndTime(end);
        meeting.setHoster(hoster);
        meeting.setPlace(place);
        meeting.setDeleted(false);
        for (Integer member : members) {
            User user = userService.getUserById(member);
            meeting.addMember(user);
            user.joinMeeting(meeting);
        }

        place.AddMeeting(meeting);
        return meetingRepository.save(meeting);
    }

    public Meeting getMeetingById(Integer id) {
        return meetingRepository.findByIdAndIsDeletedFalse(id);
    }

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findByIsDeletedFalse();
    }

    public Long countMeetings() {
        return meetingRepository.countByIsDeletedFalse();
    }

    public Meeting updateMeeting(Integer id, String title, String description, LocalDateTime start, LocalDateTime end) throws Exception {
        Meeting meeting = meetingRepository.findByIdAndIsDeletedFalse(id);
        if (meeting != null) {
            if (title != null) meeting.setTitle(title);
            if (description != null) meeting.setDescription(description);
            if (start != null) meeting.setStartTime(start);
            if (end != null) meeting.setEndTime(end);
            return meetingRepository.save(meeting);
        } else {
            throw new Exception("未查找到对应会议数据");
        }
    }

    public Meeting addMember(Integer id, Integer member) throws Exception {
        Meeting meeting = meetingRepository.findByIdAndIsDeletedFalse(id);
        if (meeting != null && userService.getUserById(member) != null) {
            meeting.addMember(userService.getUserById(member));
            return meetingRepository.save(meeting);
        } else {
            throw new Exception("未查找到对应会议数据");
        }
    }

    public Meeting removeMember(Integer id, Integer member) throws Exception {
        Meeting meeting = meetingRepository.findByIdAndIsDeletedFalse(id);
        if (meeting != null && userService.getUserById(member) != null) {
            meeting.removeMember(userService.getUserById(member));
            return meetingRepository.save(meeting);
        } else {
            throw new Exception("未查找到对应会议数据");
        }
    }

    public Meeting updateMembers(Integer id, List<Integer> members) throws Exception {
        Meeting meeting = meetingRepository.findByIdAndIsDeletedFalse(id);
        if (meeting != null) {
            for (User member : meeting.getMembers()) {
                meeting.removeMember(member);
            }
            for (Integer member : members) {
                if(userService.getUserById(member) != null)
                    meeting.addMember(userService.getUserById(member));
            }
            return meetingRepository.save(meeting);
        } else {
            throw new Exception("未查找到对应会议数据");
        }
    }

    public Meeting deleteMeeting(Integer id) {
        Meeting meeting = meetingRepository.findByIdAndIsDeletedFalse(id);
        if (meeting != null) {
            meeting.setDeleted(true);
            return meetingRepository.save(meeting);
        } else {
            return null;
        }
    }


    public List<Meeting> searchMeetingsByTitle(String title, Integer page, Integer pageSize) {
        return meetingRepository.findByTitleContainingAndIsDeletedFalse(title, PageRequest.of(page, pageSize)).getContent();
    }

    public List<Meeting> searchMeetingsByHoster(User hoster, Integer page, Integer pageSize) {
        return meetingRepository.findByHosterAndIsDeletedFalse(hoster, PageRequest.of(page, pageSize)).getContent();
    }

    public List<Meeting> searchMeetingsByPlace(MeetingRoom place, Integer page, Integer pageSize) {
        return meetingRepository.findByPlaceAndIsDeletedFalse(place, PageRequest.of(page, pageSize)).getContent();
    }

    public List<Meeting> searchUpcomingMeetingsByPlace(MeetingRoom place, Integer page, Integer pageSize) {
        return meetingRepository.findUpcomingMeetingsByPlaceAndIsDeletedFalse(place, PageRequest.of(page, pageSize)).getContent();
    }

    public List<Meeting> searchFinishedMeetingsByPlace(MeetingRoom place, Integer page, Integer pageSize) {
        return meetingRepository.findFinishedMeetingsByPlace(place, PageRequest.of(page, pageSize)).getContent();
    }

    public List<Meeting> searchOngoingMeetingsByPlace(MeetingRoom place, Integer page, Integer pageSize) {
        return meetingRepository.findOngoingMeetingsByPlace(place, PageRequest.of(page, pageSize)).getContent();
    }

    // 获取会议成员分页列表
    public List<User> getMeetingMembers(Integer meetingId, int pageNum, int pageSize) throws Exception {
        Meeting meeting = meetingRepository.findByIdAndIsDeletedFalse(meetingId);
        if (meeting == null) {
            throw new Exception("未查找到对应会议数据");
        }
        List<User> members = new ArrayList<>(meeting.getMembers());
        int total = members.size();
        int fromIndex = Math.max(0, (pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize, total);
        if (fromIndex > toIndex) {
            return Collections.emptyList();
        }
        return members.subList(fromIndex, toIndex);
    }
}
