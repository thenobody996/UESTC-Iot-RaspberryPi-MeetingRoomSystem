package com.zongshe.pack.Service;

import com.zongshe.pack.Entity.Meeting;
import com.zongshe.pack.Entity.MeetingRoom;
import com.zongshe.pack.Entity.User;
import com.zongshe.pack.Repository.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    public Meeting addMeeting(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    public Meeting getMeetingById(Integer id) {
        return meetingRepository.findByIdAndIsDeletedFalse(id);
    }

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findByIsDeletedFalse();
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

    public Meeting deleteMeeting(Integer id) {
        Meeting meeting = meetingRepository.findByIdAndIsDeletedFalse(id);
        if (meeting != null) {
            meeting.setDeleted(true);
            return meetingRepository.save(meeting);
        } else {
            return null;
        }
    }

    public List<Meeting> searchMeetingsByTitle(String title) {
        return meetingRepository.findByTitleContainingAndIsDeletedFalse(title);
    }

    public List<Meeting> searchMeetingsByHoster(User hoster) {
        return meetingRepository.findByHosterAndIsDeletedFalse(hoster);
    }

    public List<Meeting> searchMeetingsByPlace(MeetingRoom place) {
        return meetingRepository.findByPlaceAndIsDeletedFalse(place);
    }

    public List<Meeting> searchUpcomingMeetingsByPlace(MeetingRoom place) {
        return meetingRepository.findUpcomingMeetingsByPlaceAndIsDeletedFalse(place);
    }

    public List<Meeting> searchFinishedMeetingsByPlace(MeetingRoom place) {
        return meetingRepository.findFinishedMeetingsByPlace(place);
    }

    public List<Meeting> searchOngoingMeetingsByPlace(MeetingRoom place) {
        return meetingRepository.findOngoingMeetingsByPlace(place);
    }
}
