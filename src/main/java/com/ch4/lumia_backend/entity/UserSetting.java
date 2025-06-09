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

    @Column(name = "notification_interval", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'DAILY_SPECIFIC_TIME'")
    private String notificationInterval = "DAILY_SPECIFIC_TIME";

    @Column(name = "notification_time")
    private LocalTime notificationTime;

    @Column(name = "push_notification_enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean pushNotificationEnabled = true;

    // ======================= ▼▼▼ 필드 추가 및 수정 ▼▼▼ =======================
    @Column(name = "last_issued_at")
    private LocalDateTime lastIssuedAt; // 마지막으로 질문을 제공한 시간 (정기/추가 모두 포함)

    @Column(name = "last_issued_question_id")
    private Long lastIssuedQuestionId; // 마지막으로 제공한 질문의 ID

    @Column(name = "last_daily_mood_at")
    private LocalDateTime lastDailyMoodAt; // 마지막으로 '추가 질문(DAILY_MOOD)'을 받은 시간
    // ======================= ▲▲▲ 필드 추가 및 수정 ▲▲▲ =======================


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 아래 필드들은 이제 사용하지 않으므로 주석 처리 또는 삭제합니다.
    // @Column(name = "last_scheduled_message_at")
    // private LocalDateTime lastScheduledMessageAt;
    //
    // @Column(name = "in_app_notification_enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    // private boolean inAppNotificationEnabled = true;


    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Builder
    public UserSetting(User user, String notificationInterval, LocalTime notificationTime, boolean pushNotificationEnabled, LocalDateTime lastIssuedAt, Long lastIssuedQuestionId, LocalDateTime lastDailyMoodAt) {
        this.user = user;
        this.notificationInterval = notificationInterval;
        this.notificationTime = notificationTime;
        this.pushNotificationEnabled = pushNotificationEnabled;
        this.lastIssuedAt = lastIssuedAt;
        this.lastIssuedQuestionId = lastIssuedQuestionId;
        this.lastDailyMoodAt = lastDailyMoodAt;
    }
}