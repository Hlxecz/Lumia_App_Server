// src/main/java/com/ch4/lumia_backend/service/RefreshTokenService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.entity.RefreshToken;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.repository.RefreshTokenRepository;
import com.ch4.lumia_backend.repository.UserRepository;
import com.ch4.lumia_backend.security.jwt.JwtUtil; // JwtUtil 주입
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Logger 추가
import org.slf4j.LoggerFactory; // LoggerFactory 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    // Logger 추가
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh.token.validity.ms}")
    private Long refreshTokenDurationMs;

    /**
     * 지정된 사용자에 대한 리프레시 토큰을 생성하거나 업데이트합니다.
     * 기존 토큰이 있으면 해당 토큰의 값과 만료 시간을 업데이트합니다.
     * 기존 토큰이 없으면 새로 생성하여 저장합니다.
     * @param userId 리프레시 토큰을 생성/업데이트할 사용자의 ID
     * @return 생성되거나 업데이트된 RefreshToken 엔티티
     */
    @Transactional
    public RefreshToken createOrUpdateRefreshToken(String userId) { // 메소드 이름 변경 (선택 사항)
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID {} for creating/updating refresh token.", userId);
                    return new RuntimeException("Error: User not found with ID " + userId + " for creating/updating refresh token.");
                });

        String newTokenValue = jwtUtil.generateRefreshToken(userId);
        Instant newExpiryDate = Instant.now().plusMillis(refreshTokenDurationMs);

        // 해당 사용자의 기존 리프레시 토큰을 찾음
        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUser(user);

        RefreshToken refreshTokenToSave;
        if (existingTokenOpt.isPresent()) {
            // 기존 토큰이 있으면, 해당 토큰의 값과 만료 시간을 업데이트
            refreshTokenToSave = existingTokenOpt.get();
            refreshTokenToSave.setToken(newTokenValue); // RefreshToken 엔티티에 setToken 메소드 필요
            refreshTokenToSave.setExpiryDate(newExpiryDate); // RefreshToken 엔티티에 setExpiryDate 메소드 필요
            logger.info("Updating existing refresh token for user ID: {}", userId);
        } else {
            // 기존 토큰이 없으면, 새로 생성
            refreshTokenToSave = new RefreshToken(user, newTokenValue, newExpiryDate);
            logger.info("Creating new refresh token for user ID: {}", userId);
        }
        
        // save 메소드는 엔티티가 새 것이면 INSERT, 이미 존재하면 (Id로 찾아) UPDATE를 실행
        return refreshTokenRepository.save(refreshTokenToSave);
    }

    /**
     * 제공된 토큰 문자열로 RefreshToken 엔티티를 찾습니다.
     * @param token 찾을 리프레시 토큰 문자열
     * @return Optional<RefreshToken>
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * 제공된 RefreshToken이 만료되었는지 확인하고, 만료되었다면 DB에서 삭제 후 예외를 발생시킵니다.
     * @param token 검증할 RefreshToken 엔티티
     * @return 만료되지 않았다면 동일한 RefreshToken 엔티티
     * @throws RuntimeException 리프레시 토큰이 만료된 경우
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            logger.warn("Refresh token ID {} for user ID {} expired at {}. Deleting.", 
                        token.getId(), token.getUser().getUserId(), token.getExpiryDate());
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Error: Refresh token '" + token.getToken() + "' was expired. Please make a new signin request.");
        }
        return token;
    }

    /**
     * 특정 사용자의 모든 리프레시 토큰을 DB에서 삭제합니다. (예: 로그아웃 시 호출)
     * @param userId 토큰을 삭제할 사용자의 ID
     */
    @Transactional
    public void deleteByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                     logger.error("User not found with ID {} for deleting refresh tokens.", userId);
                     return new RuntimeException("Error: User not found with ID " + userId + " for deleting refresh tokens.");
                });
        refreshTokenRepository.deleteByUser(user); // void 반환 타입에 맞게 수정
        logger.info("Attempted to delete refresh token(s) for user ID: {}", userId); // 로그 메시지 변경 (삭제된 수를 알 수 없으므로)
    }

    // 기존 createRefreshToken 메소드는 createOrUpdateRefreshToken으로 대체되었으므로,
    // UserController에서 호출하는 부분도 변경해야 합니다.
    // 만약 기존 메소드 이름을 유지하고 싶다면, createRefreshToken 내부 로직을 위와 같이 수정하면 됩니다.
    // 여기서는 메소드 이름을 createOrUpdateRefreshToken으로 변경했다고 가정합니다.
}