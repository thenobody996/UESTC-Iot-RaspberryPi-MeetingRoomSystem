package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.Meeting;
import com.zongshe.pack.Entity.MeetingRoom;
import com.zongshe.pack.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Integer> {

    List<Meeting> findByIsDeletedFalse();

    Meeting findByIdAndIsDeletedFalse(Integer id);

    List<Meeting> findByTitleContainingAndIsDeletedFalse(String title);

    List<Meeting> findByPlaceAndIsDeletedFalse(MeetingRoom place);

    @Query("SELECT m FROM Meeting m WHERE m.place = :place AND m.isDeleted = false AND m.startTime > CURRENT_TIMESTAMP")
    List<Meeting> findUpcomingMeetingsByPlaceAndIsDeletedFalse(@Param("place")MeetingRoom place);

    @Query("SELECT m FROM Meeting m WHERE m.place = :place AND m.isDeleted = false AND m.endTime < CURRENT_TIMESTAMP")
    List<Meeting> findFinishedMeetingsByPlace(@Param("place")MeetingRoom place);

    @Query("SELECT m FROM Meeting m WHERE m.place = :place AND m.isDeleted = false AND m.startTime <= CURRENT_TIMESTAMP AND m.endTime >= CURRENT_TIMESTAMP")
    List<Meeting> findOngoingMeetingsByPlace(@Param("place")MeetingRoom place);

    List<Meeting> findByHosterAndIsDeletedFalse(User hoster);
}
