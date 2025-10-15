package com.zongshe.pack.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
