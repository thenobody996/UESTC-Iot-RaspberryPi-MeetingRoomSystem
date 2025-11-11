package com.zongshe.pack.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "meeting")
public class Meeting {
    /**
     * id
     */
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * hoster,会议主持人,通过id关联user
     */
    @Setter
    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User hoster;
    /**
     * place,会议地点,通过id关联meetingroom
     */
    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MeetingRoom place;

    @Setter
    @Getter
    @Column(name = "title")
    private String title;

    @Setter
    @Getter
    @Column(name = "description")
    private String description;

    @Setter
    @Getter
    @Column(name = "start")
    private LocalDateTime startTime;

    @Setter
    @Getter
    @Column(name = "end")
    private LocalDateTime endTime;

    @Setter
    @Getter
    @Column(name = "create_at" , updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime create_At;

    @Setter
    @Getter
    @Column(name = "update_at")
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime update_At;

    @Column(name = "is_deleted")
    private Boolean isDeleted;


    @Getter
    @ManyToMany
    @JoinTable(
            name = "user_meeting",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    public void addMember(User user) {
        if (user == null) return;
        user.getMeetings().add(this); // 假设 User 有 getMeetings()
    }

    public void removeMember(User user) {
        if (user == null) return;
        user.getMeetings().remove(this); // 假设 User 有 getMeetings()
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
