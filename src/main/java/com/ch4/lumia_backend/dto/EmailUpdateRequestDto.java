// src/main/java/com/ch4/lumia_backend/dto/EmailUpdateRequestDto.java
package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailUpdateRequestDto {
    private String newEmail;
    // private String currentPassword; // 필요시 현재 비밀번호 확인용
}