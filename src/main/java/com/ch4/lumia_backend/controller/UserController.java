// src/main/java/com/ch4/lumia_backend/controller/UserController.java
package com.ch4.lumia_backend.controller;

import com.ch4.lumia_backend.dto.*;
import com.ch4.lumia_backend.entity.User; // User 엔티티가 필요하다면 사용 (보통 DTO로 반환)
import com.ch4.lumia_backend.security.jwt.JwtUtil; // JWT 생성에 필요
import com.ch4.lumia_backend.service.UserService;
import com.ch4.lumia_backend.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserSettingService userSettingService;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        logger.info("Login attempt for user: {}", loginRequestDto.getUserId());
        boolean loginSuccess = userService.login(loginRequestDto.getUserId(), loginRequestDto.getPassword());
        if (loginSuccess) {
            String token = jwtUtil.generateToken(loginRequestDto.getUserId());
            logger.info("Login successful for user: {}, token generated.", loginRequestDto.getUserId());
            LoginResponseDto loginResponse = new LoginResponseDto(token, loginRequestDto.getUserId(), "로그인 성공!");
            return ResponseEntity.ok(loginResponse);
        } else {
            logger.warn("Login failed for user: {}", loginRequestDto.getUserId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto signupRequestDto) {
        logger.info("Signup attempt for user: {}", signupRequestDto.getUserId());
        try {
            User savedUser = userService.signup(signupRequestDto);
            logger.info("Signup successful for user: {}", savedUser.getUserId());
            // 회원가입 성공 메시지 또는 생성된 사용자 정보 일부 반환 가능
            return ResponseEntity.status(HttpStatus.CREATED)
                                 .body(savedUser.getUserId() + " 님 회원가입 성공!");
        } catch (IllegalArgumentException e) {
            logger.warn("Signup failed for user {}: {}", signupRequestDto.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error during signup for user {}: {}", signupRequestDto.getUserId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/users/me/settings")
    public ResponseEntity<?> getUserSettings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName(); // JWT 토큰의 subject (사용자 ID)

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            logger.warn("Attempt to get user settings without authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }
        logger.info("Fetching settings for user: {}", currentUserId);
        try {
            UserSettingDto settingsDto = userSettingService.getUserSettings(currentUserId);
            return ResponseEntity.ok(settingsDto);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get settings for user {}: {}", currentUserId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching settings for user {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("설정 조회 중 오류 발생");
        }
    }

    @PutMapping("/users/me/settings")
    public ResponseEntity<?> updateUserSettings(@RequestBody UserSettingDto userSettingDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            logger.warn("Attempt to update user settings without authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }
        logger.info("Updating settings for user: {}", currentUserId);
        try {
            UserSettingDto updatedSettings = userSettingService.updateUserSettings(currentUserId, userSettingDto);
            logger.info("Settings updated successfully for user: {}", currentUserId);
            return ResponseEntity.ok(updatedSettings);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update settings for user {}: {}", currentUserId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating settings for user {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("설정 업데이트 중 오류 발생");
        }
    }
}