// src/main/java/com/ch4/lumia_backend/dto/UserProfileResponseDto.java
package com.ch4.lumia_backend.dto;

import com.ch4.lumia_backend.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileResponseDto {
    private String loginId;
    private String email;
    private String username;
    private String gender;
    private String bloodType;
    private String mbti;

    public static UserProfileResponseDto fromEntity(User user) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setLoginId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setGender(user.getGender());
        dto.setBloodType(user.getBloodType());
        dto.setMbti(user.getMbti());
        return dto;
    }
}