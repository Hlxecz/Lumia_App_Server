// lumin/src/main/java/com/ch4/lumia_backend/security/jwt/JwtUtil.java 수정본
package com.ch4.lumia_backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // 이 import 문이 중복되지 않도록 확인 (이미 있다면 그대로 둡니다)
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Pattern;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret.key:YourVeryLongAndSecureSecretKeyForLumiaAppReplaceThis}")
    private String secretKeyPlain;

    private SecretKey secretKey;

    // application.properties에서 Access Token 유효 기간을 읽어옵니다.
    // 값이 없거나 잘못된 경우 기본값으로 1시간(3600000ms)을 사용합니다.
    @Value("${jwt.access.token.validity.ms:3600000}")
    private long accessTokenValidityInMilliseconds;

    // application.properties에서 Refresh Token 유효 기간을 읽어옵니다.
    // 값이 없거나 잘못된 경우 기본값으로 30일(2592000000ms)을 사용합니다.
    @Value("${jwt.refresh.token.validity.ms:2592000000}")
    private long refreshTokenValidityInMilliseconds;

    // 간단한 Base64 형식 체크를 위한 정규표현식 (엄밀한 검사는 아님)
    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]*={0,2}$");

    private boolean isBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.length() % 4 == 0 && BASE64_PATTERN.matcher(str).matches();
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        if (isBase64(secretKeyPlain)) {
            try {
                keyBytes = Base64.getDecoder().decode(secretKeyPlain);
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to Base64 decode JWT Secret Key, treating as plain string (UTF-8). " +
                            "Ensure it meets length requirements for the algorithm.", e);
                keyBytes = secretKeyPlain.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            logger.info("JWT Secret Key is not Base64 encoded. Using plain string bytes (UTF-8). " +
                        "Ensure it meets length requirements for the algorithm.");
            keyBytes = secretKeyPlain.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) { // HS256 최소 32바이트 (256비트)
            logger.warn("Provided JWT secret key is too short ({} bytes). HS256 requires at least 32 bytes. ", keyBytes.length);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성 메소드
    // (이름을 generateAccessToken으로 변경하는 것을 고려해볼 수 있습니다.)
    public String generateToken(String userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.accessTokenValidityInMilliseconds); // 주입받은 Access Token 유효 기간 사용

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성 메소드 (새로 추가)
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        // Refresh Token의 만료 시간은 refreshTokenValidityInMilliseconds 변수를 사용합니다.
        Date validity = new Date(now.getTime() + this.refreshTokenValidityInMilliseconds); // 주입받은 Refresh Token 유효 기간 사용

        return Jwts.builder()
                .subject(userId) // Access Token과 마찬가지로 사용자 ID를 포함할 수 있습니다.
                // 필요하다면 다른 claim을 추가하여 Access Token과 구분할 수 있습니다.
                // 예: .claim("type", "refresh") 
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey) // 동일한 secretKey를 사용하거나, Refresh Token용 별도 키를 사용할 수도 있습니다.
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            // Access Token이 만료된 것은 일반적인 상황이므로, 로그 레벨을 info 또는 debug로 낮추는 것을 고려해볼 수 있습니다.
            // 또는 이 예외를 호출하는 쪽(예: JwtAuthenticationFilter)에서 특별히 처리하도록 할 수도 있습니다.
            logger.info("Expired JWT token: {}", e.getMessage()); // 로그 레벨 변경 고려
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty or invalid: {}", e.getMessage());
        }
        return false;
    }
}