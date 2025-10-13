package com.zongshe.pack.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting")
public class Meeting {
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * hoster,会议主持人,通过id关联user
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User hoster;
    /**
     * place,会议地点,通过id关联meetingroom
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MeetingRoom place;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start")
    private LocalDateTime startTime;

    @Column(name = "end")
    private LocalDateTime endTime;

    @Column(name = "create_at" , updatable = false)
    private LocalDateTime create_At;

    @Column(name = "update_at")
    private LocalDateTime update_At;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getHoster() {
        return hoster;
    }

    public void setHoster(User hoster) {
        this.hoster = hoster;
    }

    public MeetingRoom getPlace() {
        return place;
    }

    public void setPlace(MeetingRoom place) {
        this.place = place;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreate_At() {
        return create_At;
    }

    public void setCreate_At(LocalDateTime create_At) {
        this.create_At = create_At;
    }

    public LocalDateTime getUpdate_At() {
        return update_At;
    }

    public void setUpdate_At(LocalDateTime update_At) {
        this.update_At = update_At;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
