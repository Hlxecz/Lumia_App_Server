// src/main/java/com/ch4/lumia_backend/dto/UserProfileUpdateRequestDto.java
package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequestDto {
    private String username; // 닉네임
    private String gender;
    private String bloodType;
    private String mbti;
}