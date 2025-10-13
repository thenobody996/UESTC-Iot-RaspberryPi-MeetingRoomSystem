package com.zongshe.pack.Repository;

import com.zongshe.pack.Entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Integer> {
}
