// src/main/java/com/ch4/lumia_backend/dto/PasswordUpdateRequestDto.java
package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateRequestDto {
    private String currentPassword;
    private String newPassword;
}