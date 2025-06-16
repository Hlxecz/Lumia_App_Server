// src/main/java/com/ch4/lumia_backend/dto/UserSettingDto.java
package com.ch4.lumia_backend.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ch4.lumia_backend.entity.UserSetting;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSettingDto {
    private String notificationInterval;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime notificationTime;
    
    private Boolean pushNotificationEnabled;
    
    private LocalDateTime lastIssuedAt; // 마지막 질문 제공 시간

    // Entity -> DTO 변환 메소드
    public static UserSettingDto fromEntity(UserSetting entity) {
        UserSettingDto dto = new UserSettingDto();
        dto.setNotificationInterval(entity.getNotificationInterval());
        dto.setNotificationTime(entity.getNotificationTime());
        dto.setPushNotificationEnabled(entity.isPushNotificationEnabled());
        dto.setLastIssuedAt(entity.getLastIssuedAt());
        
        return dto;
    }
}