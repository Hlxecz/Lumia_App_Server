// src/main/java/com/ch4/lumia_backend/dto/UserIdResponseDto.java
package com.ch4.lumia_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 생성자를 통해 userId를 받기 위함
public class UserIdResponseDto {
    private String userId;
}