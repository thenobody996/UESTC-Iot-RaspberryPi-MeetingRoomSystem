package com.zongshe.pack.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class MeetingRequest {

    private Integer host_id;

    private Integer place_id;

    private String title;

    private String description;

    private LocalDateTime start_time;

    private LocalDateTime end_time;

    private List<Integer> members_id;
}
