package com.zongshe.pack.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "meetingroom")
public class MeetingRoom {
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Getter
    @Column(name = "name")
    private String name;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id",referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User manager;

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

    @Getter
    @Column(name = "create_at" , updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createAt;

    @Setter
    @Getter
    @Column(name = "update_at")
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "place"})
    private List<Meeting> meetings;

    public void AddMeeting(Meeting meeting){
        this.meetings.add(meeting);
    }

    public void RemoveMeeting(Meeting meeting){
        this.meetings.remove(meeting);
    }

    @Getter
    @Setter
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
