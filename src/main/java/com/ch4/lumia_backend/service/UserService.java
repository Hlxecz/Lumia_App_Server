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
import org.springframework.util.StringUtils; // StringUtils 추가

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
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
    
 // PostController에서 User 엔티티를 직접 사용하기 위한 메소드 추가
    @Transactional(readOnly = true)
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with userId: {}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
                });
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
                // gender, bloodType, mbti는 null로 초기화 (프로필에서 설정)
                .build();

        try {
            User savedUser = userRepository.save(newUser);
            logger.info("User {} signed up successfully.", savedUser.getUserId());


            UserSetting defaultSettings = UserSetting.builder()
                    .user(savedUser)
                    .notificationInterval("WHEN_APP_OPENS") // UserSetting 엔티티의 실제 필드값
                    .inAppNotificationEnabled(true)          // UserSetting 엔티티의 실제 필드값
                    .pushNotificationEnabled(true)           // UserSetting 엔티티의 실제 필드값
                    // UserSetting 엔티티에 notificationTime, lastScheduledMessageAt 필드가 있다면
                    // 필요에 따라 기본값 설정 또는 null로 두어도 됩니다.
                    // .notificationTime(null) // 예시
                    // .lastScheduledMessageAt(null) // 예시
                    .build();
            userSettingRepository.save(defaultSettings);
            logger.info("Default settings created for user {}.", savedUser.getUserId());


            return savedUser;
        } catch (DataIntegrityViolationException e) {
            // Unique 제약 조건 위반 (userId, email)
            // 이미 위에서 확인했지만, 동시에 요청이 들어올 경우를 대비
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

        // 닉네임(username) 업데이트: DTO의 username 필드가 제공되었고(null 아님), 실제 값이 있는 문자열이며, 기존 값과 다를 때 업데이트
        if (profileDto.getUsername() != null) {
            if (StringUtils.hasText(profileDto.getUsername())) { // null, "", " " 모두 false
                if (user.getUsername() == null || !user.getUsername().equals(profileDto.getUsername())) {
                    user.setUsername(profileDto.getUsername());
                    isProfileUpdated = true;
                    logger.info("For user {}: Username updated to: {}", userLoginId, profileDto.getUsername());
                }
            } else { // username 키는 있는데 값이 "" (빈 문자열)인 경우
                 logger.warn("For user {}: Attempt to set username to empty string. This is ignored as username should not be empty.", userLoginId);
            }
        }

        // 성별 업데이트: DTO의 gender 필드가 null이 아닐 때만 고려. 빈 문자열 ""은 null로 취급하여 DB에 저장 (값 비우기 허용)
        if (profileDto.getGender() != null) {
            String newGender = profileDto.getGender().isEmpty() ? null : profileDto.getGender();
            // 현재 값과 새로운 값을 비교 (둘 다 null이거나, 둘 다 값이 있고 같은 경우는 변경 안 함)
            if (user.getGender() == null ? (newGender != null) : !user.getGender().equals(newGender)) {
                user.setGender(newGender);
                isProfileUpdated = true;
                logger.info("For user {}: Gender updated to: {}", userLoginId, newGender);
            }
        }

        // 혈액형 업데이트: DTO의 bloodType 필드가 null이 아닐 때만 고려. 빈 문자열 ""은 null로 취급.
        if (profileDto.getBloodType() != null) {
            String newBloodType = profileDto.getBloodType().isEmpty() ? null : profileDto.getBloodType();
            if (user.getBloodType() == null ? (newBloodType != null) : !user.getBloodType().equals(newBloodType)) {
                user.setBloodType(newBloodType);
                isProfileUpdated = true;
                logger.info("For user {}: BloodType updated to: {}", userLoginId, newBloodType);
            }
        }

        // MBTI 업데이트: DTO의 mbti 필드가 null이 아닐 때만 고려. 빈 문자열 ""은 null로 취급.
        if (profileDto.getMbti() != null) {
            String newMbti = profileDto.getMbti().isEmpty() ? null : profileDto.getMbti();
            // MBTI는 대문자로 변환하여 저장할 수 있습니다. 예: newMbti = (newMbti != null) ? newMbti.toUpperCase() : null;
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
            return UserProfileResponseDto.fromEntity(user); // 변경 사항이 없어도 현재 사용자 정보 반환
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
        // 이메일 형식 유효성 검사 (정규식 사용 등) 추가 가능
        // if (!isValidEmailFormat(newEmail)) {
        //     throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        // }

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
        
        // 새 비밀번호 유효성 검사 (예: 길이, 포함 문자 등)
        if (passwordDto.getNewPassword().length() < 8) { // 예시: 최소 8자
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }
        if (passwordDto.getNewPassword().equals(passwordDto.getCurrentPassword())) { // 현재 비밀번호와 새 비밀번호가 같은 경우
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
        logger.info("Password for user {} updated successfully.", userLoginId);
    }
}