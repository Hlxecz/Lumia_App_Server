// src/main/java/com/ch4/lumia_backend/dto/TokenRefreshResponseDto.java
package com.ch4.lumia_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenRefreshResponseDto {
    private String accessToken;
    private String refreshToken; // 선택: 리프레시 토큰 순환(rotation)을 적용할 경우 새 리프레시 토큰도 함께 반환
}