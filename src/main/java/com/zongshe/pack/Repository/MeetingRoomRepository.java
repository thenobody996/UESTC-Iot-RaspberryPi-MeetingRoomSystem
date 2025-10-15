package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Integer> {

}
