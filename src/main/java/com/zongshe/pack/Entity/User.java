package com.zongshe.pack.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
public class User {
    // Getters and Setters
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Getter
    @NotNull(message = "用户名不能为空")
    @Column(name = "account")
    private String account;

    @Setter
    @Getter
    @NotNull(message = "密码不能为空")
    @Column(name = "password")
    private String password;

    @Setter
    @Getter
    @Column(nullable = false, columnDefinition = "ENUM('admin','user') DEFAULT 'user' COMMENT '角色：管理员或普通用户'")
    private String role = "user";

    // 创建时间（自动设置）
    @Setter
    @Getter
    @Column(name = "created_at", updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    // 更新时间（每次更新自动设置）
    @Setter
    @Getter
    @Column(name = "updated_at")
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;

    // 逻辑删除标志（false = 未删除，true = 已删除）
    @Column(name = "is_deleted",nullable = false)
    private Boolean isDeleted = false;

    @ManyToMany
    @JoinTable(
            name = "user_meeting",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "meeting_id")
    )
    private List<Meeting> meetings;

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
