// src/main/java/com/ch4/lumia_backend/dto/UserSettingDto.java
package com.ch4.lumia_backend.dto;

import com.ch4.lumia_backend.entity.UserSetting;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;


@Getter
@Setter
@NoArgsConstructor
public class UserSettingDto {
    private String notificationInterval; // "NONE", "DAILY_SPECIFIC_TIME", "WHEN_APP_OPENS"

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss") // JSON 직렬화/역직렬화 시 포맷 지정
    private LocalTime notificationTime; // "HH:mm:ss"

    private Boolean inAppNotificationEnabled;
    private Boolean pushNotificationEnabled; // 향후 확장용

    // Entity -> DTO
    public static UserSettingDto fromEntity(UserSetting entity) {
        UserSettingDto dto = new UserSettingDto();
        dto.setNotificationInterval(entity.getNotificationInterval());
        dto.setNotificationTime(entity.getNotificationTime());
        dto.setInAppNotificationEnabled(entity.isInAppNotificationEnabled());
        dto.setPushNotificationEnabled(entity.isPushNotificationEnabled());
        return dto;
    }
}