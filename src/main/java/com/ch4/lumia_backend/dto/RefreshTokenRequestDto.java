// src/main/java/com/ch4/lumia_backend/dto/RefreshTokenRequestDto.java
package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor // JSON 바인딩을 위해 기본 생성자 필요
public class RefreshTokenRequestDto {
    private String refreshToken;
}