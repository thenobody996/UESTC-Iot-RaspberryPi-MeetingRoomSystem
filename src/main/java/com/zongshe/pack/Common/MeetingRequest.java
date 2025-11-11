package com.zongshe.pack.Common;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingRequest {

    @Getter
    @Setter
    private Integer host_id;

    @Getter
    @Setter
    private Integer place_id;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private LocalDateTime start_time;

    @Getter
    @Setter
    private LocalDateTime end_time;

    @Getter
    @Setter
    private List<Integer> members_id;
}
