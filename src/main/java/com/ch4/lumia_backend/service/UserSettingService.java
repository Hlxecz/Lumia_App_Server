// src/main/java/com/ch4/lumia_backend/service/UserSettingService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.UserSettingDto;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserSetting;
import com.ch4.lumia_backend.repository.UserRepository;
import com.ch4.lumia_backend.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserSettingDto getUserSettings(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 설정이 없는 경우, 기본 설정을 생성하여 반환 (UserService의 signup에서 이미 생성하므로, 방어 로직으로 간주)
        UserSetting userSetting = userSettingRepository.findByUser(user)
                .orElseGet(() -> {
                    UserSetting defaultSettings = UserSetting.builder()
                            .user(user)
                            .notificationInterval("WHEN_APP_OPENS") // 기본값
                            .inAppNotificationEnabled(true)
                            .pushNotificationEnabled(true)
                            .build();
                    return userSettingRepository.save(defaultSettings);
                });
        return UserSettingDto.fromEntity(userSetting);
    }

    @Transactional
    public UserSettingDto updateUserSettings(String userId, UserSettingDto userSettingDto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        UserSetting userSetting = userSettingRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("사용자 설정을 찾을 수 없습니다. 회원가입 시 생성되어야 합니다."));

        // DTO에서 받은 값으로 업데이트
        if (userSettingDto.getNotificationInterval() != null) {
            userSetting.setNotificationInterval(userSettingDto.getNotificationInterval());
        }
        // notificationTime은 "HH:mm:ss" 문자열로 올 수 있으므로 LocalTime으로 파싱 필요 (DTO에서 LocalTime 타입이면 그대로 사용)
        if (userSettingDto.getNotificationTime() != null) {
            userSetting.setNotificationTime(userSettingDto.getNotificationTime());
        } else if ("NONE".equals(userSettingDto.getNotificationInterval()) || "WHEN_APP_OPENS".equals(userSettingDto.getNotificationInterval())) {
            // 특정 시간이 필요 없는 간격의 경우 notificationTime을 null로 설정
            userSetting.setNotificationTime(null);
        }

        if (userSettingDto.getInAppNotificationEnabled() != null) {
            userSetting.setInAppNotificationEnabled(userSettingDto.getInAppNotificationEnabled());
        }
        if (userSettingDto.getPushNotificationEnabled() != null) {
            userSetting.setPushNotificationEnabled(userSettingDto.getPushNotificationEnabled());
        }
        // UserSetting 엔티티의 @PreUpdate가 updatedAt을 자동으로 업데이트함

        UserSetting updatedSetting = userSettingRepository.save(userSetting);
        return UserSettingDto.fromEntity(updatedSetting);
    }
}