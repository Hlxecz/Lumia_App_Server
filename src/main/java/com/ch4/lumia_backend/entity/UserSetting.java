// src/main/java/com/ch4/lumia_backend/entity/UserSetting.java
package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk_id", nullable = false, unique = true)
    private User user;

    @Column(name = "notification_interval", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'NONE'")
    private String notificationInterval = "NONE"; // "NONE", "DAILY_SPECIFIC_TIME", "WHEN_APP_OPENS"

    @Column(name = "notification_time") // HH:mm:ss
    private LocalTime notificationTime;

    @Column(name = "last_scheduled_message_at")
    private LocalDateTime lastScheduledMessageAt;

    @Column(name = "in_app_notification_enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean inAppNotificationEnabled = true;

    @Column(name = "push_notification_enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean pushNotificationEnabled = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Builder
    public UserSetting(User user, String notificationInterval, LocalTime notificationTime, LocalDateTime lastScheduledMessageAt, boolean inAppNotificationEnabled, boolean pushNotificationEnabled) {
        this.user = user;
        this.notificationInterval = notificationInterval;
        this.notificationTime = notificationTime;
        this.lastScheduledMessageAt = lastScheduledMessageAt;
        this.inAppNotificationEnabled = inAppNotificationEnabled;
        this.pushNotificationEnabled = pushNotificationEnabled;
    }
}