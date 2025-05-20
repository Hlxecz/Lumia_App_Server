package com.ch4.lumia_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
public class LoginResponseDto {
    private String token;
    private String userId;
    private String message;
    // 필요하다면 다른 사용자 정보 (예: username)도 추가 가능
}