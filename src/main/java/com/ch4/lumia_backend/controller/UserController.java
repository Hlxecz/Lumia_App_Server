// src/main/java/com/ch4/lumia_backend/controller/UserController.java
package com.ch4.lumia_backend.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ch4.lumia_backend.dto.CoinUpdateRequestDto;
import com.ch4.lumia_backend.dto.EmailUpdateRequestDto;
import com.ch4.lumia_backend.dto.EquippedItemsUpdateRequestDto;
import com.ch4.lumia_backend.dto.LoginRequestDto;
import com.ch4.lumia_backend.dto.LoginResponseDto;
import com.ch4.lumia_backend.dto.PasswordUpdateRequestDto;
import com.ch4.lumia_backend.dto.RefreshTokenRequestDto;
import com.ch4.lumia_backend.dto.SignupRequestDto;
import com.ch4.lumia_backend.dto.TokenRefreshResponseDto;
import com.ch4.lumia_backend.dto.UserIdResponseDto;
import com.ch4.lumia_backend.dto.UserProfileResponseDto;
import com.ch4.lumia_backend.dto.UserProfileUpdateRequestDto;
import com.ch4.lumia_backend.dto.UserSettingDto;
import com.ch4.lumia_backend.entity.RefreshToken;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.security.jwt.JwtUtil;
import com.ch4.lumia_backend.service.RefreshTokenService;
import com.ch4.lumia_backend.service.UserService;
import com.ch4.lumia_backend.service.UserSettingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users") // 기본 경로 일관성 유지
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserSettingService userSettingService;
    private final RefreshTokenService refreshTokenService;

    // === 인증 관련 엔드포인트 ===
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        logger.info("Login attempt for user: {}", loginRequestDto.getUserId());
        boolean loginSuccess = userService.login(loginRequestDto.getUserId(), loginRequestDto.getPassword());
        if (loginSuccess) {
            String accessToken = jwtUtil.generateToken(loginRequestDto.getUserId());
            RefreshToken refreshTokenEntity = refreshTokenService.createOrUpdateRefreshToken(loginRequestDto.getUserId());

            logger.info("Login successful for user: {}, token generated.", loginRequestDto.getUserId());
            LoginResponseDto loginResponse = new LoginResponseDto(
                    accessToken,
                    refreshTokenEntity.getToken(),
                    loginRequestDto.getUserId(),
                    "로그인 성공!"
            );
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

    @PostMapping("/auth/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDto requestDto) {
        String requestRefreshToken = requestDto.getRefreshToken();
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtil.generateToken(user.getUserId());
                    logger.info("New access token generated for user: {} via refresh token", user.getUserId());
                    return ResponseEntity.ok(new TokenRefreshResponseDto(newAccessToken, requestRefreshToken));
                })
                .orElseThrow(() -> {
                    logger.warn("Refresh token not found or invalid during refresh attempt: {}", requestRefreshToken);
                    // 프론트엔드에서 이 예외를 잘 처리하도록 명확한 메시지 또는 커스텀 예외 반환 고려
                    return new RuntimeException("Refresh token is not in database or invalid!");
                });
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logoutUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String currentUserId = authentication.getName();
            try {
                refreshTokenService.deleteByUserId(currentUserId);
                logger.info("User {} explicitly logged out, refresh token deleted from DB.", currentUserId);
            } catch (Exception e) {
                logger.error("Error deleting refresh token for user {} during logout: {}", currentUserId, e.getMessage(), e);
            }
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok("로그아웃 되었습니다.");
        }
        logger.warn("Logout attempt by unauthenticated or anonymous user.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그아웃할 세션 정보가 없습니다.");
    }

    // === 알림 설정 관련 엔드포인트 ===
    @GetMapping("/me/settings")
    public ResponseEntity<?> getUserSettings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            logger.warn("getUserSettings: Authentication is null or user is anonymous. Responding with 401.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }
        String currentUserId = authentication.getName();
        try {
            UserSettingDto settingsDto = userSettingService.getUserSettings(currentUserId);
            return ResponseEntity.ok(settingsDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/me/settings")
    public ResponseEntity<?> updateUserSettings(@RequestBody UserSettingDto userSettingDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            logger.warn("updateUserSettings: Authentication is null or user is anonymous. Responding with 401.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }
        String currentUserId = authentication.getName();
        try {
            UserSettingDto updatedSettings = userSettingService.updateUserSettings(currentUserId, userSettingDto);
            return ResponseEntity.ok(updatedSettings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // === 프로필 정보 관련 엔드포인트 ===
    @GetMapping("/me/profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            logger.warn("getUserProfile: Authentication is null or user is anonymous. Responding with 401.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }
        String currentUserId = authentication.getName();
        try {
            UserProfileResponseDto profileDto = userService.getUserProfile(currentUserId);
            return ResponseEntity.ok(profileDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/me/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileUpdateRequestDto profileDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            logger.warn("updateUserProfile: Authentication is null or user is anonymous. Responding with 401.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }
        String currentUserId = authentication.getName();
        try {
            UserProfileResponseDto updatedProfile = userService.updateUserProfile(currentUserId, profileDto);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // NOT_FOUND 보다는 BAD_REQUEST가 적절할 수 있음
        }
    }

    @PutMapping("/me/email")
    public ResponseEntity<?> updateUserEmail(@RequestBody EmailUpdateRequestDto emailDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            logger.warn("updateUserEmail: Authentication is null or user is anonymous. Responding with 401.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }
        String currentUserId = authentication.getName();
        try {
            userService.updateUserEmail(currentUserId, emailDto);
            return ResponseEntity.ok("이메일이 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody PasswordUpdateRequestDto passwordDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            logger.warn("updateUserPassword: Authentication is null or user is anonymous. Responding with 401.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }
        String currentUserId = authentication.getName();
        try {
            userService.updateUserPassword(currentUserId, passwordDto);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/auth/find-id")
    public ResponseEntity<?> findIdByEmail(@RequestParam("email") String email) {
        try {
            String foundUserId = userService.findUserIdByEmail(email);
            // 성공 시, 찾은 아이디를 JSON 형태로 반환
            return ResponseEntity.ok(new UserIdResponseDto(foundUserId));
        } catch (IllegalArgumentException e) {
            // 사용자를 찾지 못했을 경우, 404 Not Found 상태와 에러 메시지 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    @PutMapping("/me/equipped-items")
    public ResponseEntity<?> updateUserEquippedItems(@RequestBody EquippedItemsUpdateRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }
        String currentUserId = authentication.getName();
        
        try {
            userService.updateEquippedItems(currentUserId, dto.getEquippedItems());
            return ResponseEntity.ok("착용 아이템 정보가 업데이트되었습니다.");
        } catch (Exception e) {
            logger.error("Error updating equipped items for user {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("아이템 정보 업데이트 중 오류 발생");
        }
    }
    
    @PostMapping("/me/coins")
    public ResponseEntity<?> updateUserCoins(@RequestBody CoinUpdateRequestDto coinUpdateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }
        String currentUserId = authentication.getName();

        try {
            int newCoinValue = userService.updateUserCoins(currentUserId, coinUpdateDto.getAmount());
            return ResponseEntity.ok(Map.of("coins", newCoinValue));
        } catch (IllegalArgumentException e) {
            // 코인이 부족할 경우 400 Bad Request 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating coins for user {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("코인 정보 업데이트 중 오류 발생");
        }
    }
}