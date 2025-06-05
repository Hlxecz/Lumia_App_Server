// src/main/java/com/ch4/lumia_backend/service/UserService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.EmailUpdateRequestDto;
import com.ch4.lumia_backend.dto.PasswordUpdateRequestDto;
import com.ch4.lumia_backend.dto.SignupRequestDto;
import com.ch4.lumia_backend.dto.UserProfileResponseDto;
import com.ch4.lumia_backend.dto.UserProfileUpdateRequestDto;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserSetting;
import com.ch4.lumia_backend.repository.UserRepository;
import com.ch4.lumia_backend.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSettingRepository userSettingRepository;

    // PostController에서 User 엔티티를 직접 사용하기 위한 메소드 추가
    @Transactional(readOnly = true)
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with userId: {}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
                });
    }
    
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
        if (userRepository.findByEmail(signupRequestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        User newUser = User.builder()
                .userId(signupRequestDto.getUserId())
                .password(encodedPassword)
                .username(signupRequestDto.getUsername())
                .email(signupRequestDto.getEmail())
                .role("ROLE_USER")
                .build();

        try {
            User savedUser = userRepository.save(newUser);
            logger.info("User {} signed up successfully.", savedUser.getUserId());

            UserSetting defaultSettings = UserSetting.builder()
                    .user(savedUser)
                    .notificationInterval("WHEN_APP_OPENS")
                    .inAppNotificationEnabled(true)
                    .pushNotificationEnabled(true)
                    .build();
            userSettingRepository.save(defaultSettings);
            logger.info("Default settings created for user {}.", savedUser.getUserId());

            return savedUser;
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation during signup for userId {}: {}", signupRequestDto.getUserId(), e.getMessage());
            throw new IllegalArgumentException("아이디 또는 이메일이 이미 사용 중일 수 있습니다. 다시 시도해주세요.");
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(String userLoginId) {
        User user = userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> {
                    logger.warn("User profile not found for userId: {}", userLoginId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userLoginId);
                });
        return UserProfileResponseDto.fromEntity(user);
    }

    @Transactional
    public UserProfileResponseDto updateUserProfile(String userLoginId, UserProfileUpdateRequestDto profileDto) {
        User user = userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> {
                    logger.warn("User not found for profile update with userId: {}", userLoginId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userLoginId);
                });

        logger.info("Updating profile for user: {}. Incoming DTO: username={}, gender={}, bloodType={}, mbti={}",
                userLoginId, profileDto.getUsername(), profileDto.getGender(), profileDto.getBloodType(), profileDto.getMbti());
        logger.info("User current state before update: userId={}, username={}, gender={}, bloodType={}, mbti={}",
                user.getUserId(), user.getUsername(), user.getGender(), user.getBloodType(), user.getMbti());

        boolean isProfileUpdated = false;

        if (profileDto.getUsername() != null) {
            if (StringUtils.hasText(profileDto.getUsername())) {
                if (user.getUsername() == null || !user.getUsername().equals(profileDto.getUsername())) {
                    user.setUsername(profileDto.getUsername());
                    isProfileUpdated = true;
                    logger.info("For user {}: Username updated to: {}", userLoginId, profileDto.getUsername());
                }
            } else {
                 logger.warn("For user {}: Attempt to set username to empty string. This is ignored as username should not be empty.", userLoginId);
            }
        }

        if (profileDto.getGender() != null) {
            String newGender = profileDto.getGender().isEmpty() ? null : profileDto.getGender();
            if (user.getGender() == null ? (newGender != null) : !user.getGender().equals(newGender)) {
                user.setGender(newGender);
                isProfileUpdated = true;
                logger.info("For user {}: Gender updated to: {}", userLoginId, newGender);
            }
        }

        if (profileDto.getBloodType() != null) {
            String newBloodType = profileDto.getBloodType().isEmpty() ? null : profileDto.getBloodType();
            if (user.getBloodType() == null ? (newBloodType != null) : !user.getBloodType().equals(newBloodType)) {
                user.setBloodType(newBloodType);
                isProfileUpdated = true;
                logger.info("For user {}: BloodType updated to: {}", userLoginId, newBloodType);
            }
        }

        if (profileDto.getMbti() != null) {
            String newMbti = profileDto.getMbti().isEmpty() ? null : profileDto.getMbti();
            if (user.getMbti() == null ? (newMbti != null) : !user.getMbti().equals(newMbti)) {
                user.setMbti(newMbti);
                isProfileUpdated = true;
                logger.info("For user {}: MBTI updated to: {}", userLoginId, newMbti);
            }
        }

        if (isProfileUpdated) {
            User updatedUser = userRepository.save(user);
            logger.info("User profile saved for {}. Final state: username={}, gender={}, bloodType={}, mbti={}",
                    userLoginId, updatedUser.getUsername(), updatedUser.getGender(), updatedUser.getBloodType(), updatedUser.getMbti());
            return UserProfileResponseDto.fromEntity(updatedUser);
        } else {
            logger.info("No actual changes applied for user profile {}. Returning current state.", userLoginId);
            return UserProfileResponseDto.fromEntity(user);
        }
    }

    @Transactional
    public void updateUserEmail(String userLoginId, EmailUpdateRequestDto emailDto) {
        User user = userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> {
                     logger.warn("User not found for email update with userId: {}", userLoginId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userLoginId);
                });

        String newEmail = emailDto.getNewEmail();
        if (!StringUtils.hasText(newEmail)) {
            throw new IllegalArgumentException("새로운 이메일을 입력해주세요.");
        }

        if (!newEmail.equalsIgnoreCase(user.getEmail())) {
            Optional<User> existingUserWithNewEmail = userRepository.findByEmail(newEmail);
            if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getUserId().equals(userLoginId)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            
            user.setEmail(newEmail);
            userRepository.save(user);
            logger.info("Email for user {} updated to {}", userLoginId, newEmail);
        } else {
            logger.info("New email is the same as current for user {}. No update performed.", userLoginId);
        }
    }

    @Transactional
    public void updateUserPassword(String userLoginId, PasswordUpdateRequestDto passwordDto) {
        User user = userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> {
                    logger.warn("User not found for password update with userId: {}", userLoginId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userLoginId);
                });

        if (!StringUtils.hasText(passwordDto.getCurrentPassword()) || !StringUtils.hasText(passwordDto.getNewPassword())) {
            throw new IllegalArgumentException("현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
        }

        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        if (passwordDto.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }
        if (passwordDto.getNewPassword().equals(passwordDto.getCurrentPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
        logger.info("Password for user {} updated successfully.", userLoginId);
    }

    // ▼▼▼ 아이디 찾기 서비스 메소드 추가 ▼▼▼
    @Transactional(readOnly = true)
    public String findUserIdByEmail(String email) {
        if (!StringUtils.hasText(email)) { // 이메일 값이 비어있는지 확인
            logger.warn("Attempt to find userId with empty email.");
            throw new IllegalArgumentException("이메일 주소를 입력해주세요.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.info("No user found with email: {}", email);
                    return new IllegalArgumentException("해당 이메일로 가입된 계정을 찾을 수 없습니다.");
                });
        logger.info("User ID {} found for email: {}", user.getUserId(), email);
        return user.getUserId();
    }
    // ▲▲▲ 아이디 찾기 서비스 메소드 추가 ▲▲▲
}