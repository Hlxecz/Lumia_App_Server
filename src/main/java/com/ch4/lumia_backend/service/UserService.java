// src/main/java/com/ch4/lumia_backend/service/UserService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.SignupRequestDto;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserSetting;
import com.ch4.lumia_backend.repository.UserRepository;
import com.ch4.lumia_backend.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSettingRepository userSettingRepository;

    @Transactional(readOnly = true)
    public boolean login(String userId, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByUserId(userId);
        if (optionalUser.isPresent()) {
            User foundUser = optionalUser.get();
            return passwordEncoder.matches(rawPassword, foundUser.getPassword());
        }
        return false;
    }

    @Transactional
    public User signup(SignupRequestDto signupRequestDto) {
        if (userRepository.findByUserId(signupRequestDto.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        // User 엔티티 생성 시 BaseTimeEntity의 createdAt, updatedAt은 JPA Auditing에 의해 자동 설정됨
        User newUser = User.builder()
                .userId(signupRequestDto.getUserId())
                .password(encodedPassword)
                .username(signupRequestDto.getUsername())
                .email(signupRequestDto.getEmail())
                .role("ROLE_USER")
                .build();

        try {
            User savedUser = userRepository.save(newUser);

            // 회원가입 시 기본 UserSetting 생성
            // UserSetting의 updatedAt, createdAt은 해당 엔티티의 @PrePersist/@PreUpdate 또는 JPA Auditing으로 관리
            UserSetting defaultSettings = UserSetting.builder()
                    .user(savedUser) // 생성된 User 객체를 UserSetting에 연결
                    .notificationInterval("WHEN_APP_OPENS") // 기본값 예시
                    .inAppNotificationEnabled(true)
                    .pushNotificationEnabled(true)
                    // lastScheduledMessageAt은 처음에는 null 또는 특정 초기값
                    .build();
            userSettingRepository.save(defaultSettings); // UserSetting 저장 (중복 호출 수정)

            return savedUser;
        } catch (DataIntegrityViolationException e) {
            // 주로 unique 제약조건 위반 (userId, email)
            throw new IllegalArgumentException("아이디 또는 이메일이 이미 사용 중일 수 있습니다.");
        }
    }
}