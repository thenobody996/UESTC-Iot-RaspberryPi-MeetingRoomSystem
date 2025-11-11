package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.MeetingRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Integer> {

    Long countByIsDeletedFalse();

    Page<MeetingRoom> findByIsDeletedIsFalse(Pageable pageable);

    MeetingRoom findByIdAndIsDeletedFalse(Integer meetingRoomId);

    Page<MeetingRoom> findByNameContainingAndIsDeletedFalse(String name, Pageable pageable);
}
