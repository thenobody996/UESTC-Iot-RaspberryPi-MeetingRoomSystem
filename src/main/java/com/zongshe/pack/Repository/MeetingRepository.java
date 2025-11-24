package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.Meeting;
import com.zongshe.pack.Entity.MeetingRoom;
import com.zongshe.pack.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Integer> {

    Long countByIsDeletedFalse();

    List<Meeting> findByIsDeletedFalse();

    Meeting findByIdAndIsDeletedFalse(Integer id);

    Page<Meeting> findByTitleContainingAndIsDeletedFalse(String title,Pageable pageable);

    Page<Meeting> findByPlaceAndIsDeletedFalse(MeetingRoom place,Pageable pageable);


    @Query("SELECT m FROM Meeting m WHERE m.place = :place AND m.isDeleted = false AND m.startTime > CURRENT_TIMESTAMP")
    Page<Meeting> findUpcomingMeetingsByPlaceAndIsDeletedFalse(@Param("place")MeetingRoom place,Pageable pageable);

    @Query("SELECT m FROM Meeting m WHERE m.place = :place AND m.isDeleted = false AND m.endTime < CURRENT_TIMESTAMP")
    Page<Meeting> findFinishedMeetingsByPlace(@Param("place")MeetingRoom place,Pageable pageable);

    @Query("SELECT m FROM Meeting m WHERE m.place = :place AND m.isDeleted = false AND m.startTime <= CURRENT_TIMESTAMP AND m.endTime >= CURRENT_TIMESTAMP")
    Page<Meeting> findOngoingMeetingsByPlace(@Param("place")MeetingRoom place,Pageable pageable);

    Page<Meeting> findByHosterAndIsDeletedFalse(User hoster,Pageable pageable);
}
