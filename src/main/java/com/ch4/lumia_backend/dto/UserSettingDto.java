// src/main/java/com/ch4/lumia_backend/dto/UserSettingDto.java
package com.ch4.lumia_backend.dto;

import com.ch4.lumia_backend.entity.UserSetting;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class UserSettingDto {
    private String notificationInterval;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime notificationTime;

    // ======================= ▼▼▼ 삭제된 필드 ▼▼▼ =======================
    // private Boolean inAppNotificationEnabled;
    // ======================= ▲▲▲ 삭제된 필드 ▲▲▲ =======================
    
    private Boolean pushNotificationEnabled;

    // Entity -> DTO 변환 메소드
    public static UserSettingDto fromEntity(UserSetting entity) {
        UserSettingDto dto = new UserSettingDto();
        dto.setNotificationInterval(entity.getNotificationInterval());
        dto.setNotificationTime(entity.getNotificationTime());
        dto.setPushNotificationEnabled(entity.isPushNotificationEnabled());
        
        // ======================= ▼▼▼ 삭제된 로직 ▼▼▼ =======================
        // dto.setInAppNotificationEnabled(entity.isInAppNotificationEnabled());
        // ======================= ▲▲▲ 삭제된 로직 ▲▲▲ =======================
        
        return dto;
    }
}