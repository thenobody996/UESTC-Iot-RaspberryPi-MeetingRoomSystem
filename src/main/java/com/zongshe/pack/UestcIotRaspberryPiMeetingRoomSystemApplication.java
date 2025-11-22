package com.zongshe.pack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableWebSocketMessageBroker
@EnableScheduling
public class UestcIotRaspberryPiMeetingRoomSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(UestcIotRaspberryPiMeetingRoomSystemApplication.class, args);
    }
}

