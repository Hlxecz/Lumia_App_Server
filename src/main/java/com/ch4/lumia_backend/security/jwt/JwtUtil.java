package com.ch4.lumia_backend.security.jwt; // 실제 패키지 경로에 맞게 수정

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
// import io.jsonwebtoken.JwsHeader; // 필요시 사용
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Pattern; // Base64 체크를 위한 정규식

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret.key:YourVeryLongAndSecureSecretKeyForLumiaAppReplaceThis}")
    private String secretKeyPlain;

    private SecretKey secretKey;

    private final long tokenValidityInMilliseconds = 60 * 60 * 1000L; // 1시간

    // 간단한 Base64 형식 체크를 위한 정규표현식 (엄밀한 검사는 아님)
    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]*={0,2}$");

    private boolean isBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 길이도 Base64 인코딩의 특성 중 하나 (4의 배수)
        // 하지만 더 중요한 것은 문자셋과 패딩 문자(=)의 위치입니다.
        // 아래 정규식은 기본적인 문자셋과 패딩만 확인합니다.
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
            // 실제 운영에서는 여기서 에러를 발생시키거나 안전한 기본키로 대체하는 로직이 필요할 수 있습니다.
            // 여기서는 경고만 로깅하고 진행합니다. Keys.hmacShaKeyFor는 짧은 키에 대해 예외를 발생시킬 수 있습니다.
            // 만약 짧은 키로 인해 hmacShaKeyFor에서 에러가 발생한다면, application.properties의 키를 충분히 길게 해야 합니다.
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey) // 키에 알고리즘 정보가 내포됨 (HMAC-SHA 계열)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser() // parserBuilder() 대신 parser() 사용
                .verifyWith(secretKey) // setSigningKey(Key) 대신 사용
                .build()
                .parseSignedClaims(token) // parseClaimsJws(String) 대신 사용
                .getPayload(); // getBody() 대신 getPayload() (Jws<Claims>의 Claims 부분)
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
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty or invalid: {}", e.getMessage());
        }
        return false;
    }
}