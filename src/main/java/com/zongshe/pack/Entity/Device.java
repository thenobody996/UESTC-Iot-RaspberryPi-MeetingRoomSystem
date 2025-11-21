package com.zongshe.pack.Entity;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 设备唯一标识，例如 MAC/UUID
     */
    @Column(name = "device_uuid", nullable = false, unique = true, length = 64)
    private String deviceUuid;

    /**
     * 设备名称（可选）
     */
    @Column(name = "name", length = 100)
    private String name;

    /**
     * 设备密钥，用于鉴权
     */
    @Column(name = "secret_key", nullable = false, length = 128)
    private String secretKey;

    /**
     * 设备状态：online / offline
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('online','offline') DEFAULT 'offline'")
    private DeviceStatus status;

    /**
     * 最后心跳时间
     */
    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    /**
     * 创建时间
     */
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    /**
     * 更新时间
     */
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    /**
     * 关联会议室（多对一）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private MeetingRoom meetingRoom;

    /**
     * 创建和更新时间自动维护
     */
    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DeviceStatus.offline;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }

    /**
     * 状态枚举
     */
    public enum DeviceStatus {
        online,
        offline
    }
}
