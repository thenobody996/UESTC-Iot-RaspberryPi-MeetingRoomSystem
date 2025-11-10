package com.zongshe.pack.Common;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class MeetingRoomRequest {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer manager_id;

    @Setter
    @Getter
    @Column(name = "volume")
    private Integer volume;

    @Setter
    @Getter
    @Column(name = "description")
    private String description;

    @Getter
    @Setter
    @Column(name = "locateURL")
    private String locateURL;
}
