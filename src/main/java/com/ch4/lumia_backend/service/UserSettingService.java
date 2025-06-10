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

@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserSettingDto getUserSettings(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        UserSetting userSetting = userSettingRepository.findByUser(user)
                .orElseGet(() -> {
                    UserSetting defaultSettings = UserSetting.builder()
                            .user(user)
                            .notificationInterval("DAILY_SPECIFIC_TIME")
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
                .orElseThrow(() -> new IllegalArgumentException("사용자 설정을 찾을 수 없습니다."));

        if (userSettingDto.getNotificationInterval() != null) {
            userSetting.setNotificationInterval(userSettingDto.getNotificationInterval());
        }
        if (userSettingDto.getNotificationTime() != null) {
            userSetting.setNotificationTime(userSettingDto.getNotificationTime());
        } else if ("NONE".equals(userSettingDto.getNotificationInterval())) {
            userSetting.setNotificationTime(null);
        }

        if (userSettingDto.getPushNotificationEnabled() != null) {
            userSetting.setPushNotificationEnabled(userSettingDto.getPushNotificationEnabled());
        }

        UserSetting updatedSetting = userSettingRepository.save(userSetting);
        return UserSettingDto.fromEntity(updatedSetting);
    }
}