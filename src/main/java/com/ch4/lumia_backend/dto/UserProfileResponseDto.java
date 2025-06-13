// src/main/java/com/ch4/lumia_backend/dto/UserProfileResponseDto.java
package com.ch4.lumia_backend.dto;

import com.ch4.lumia_backend.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List; // List import 추가

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
    // ======================= ▼▼▼ 필드 추가 ▼▼▼ =======================
    private int coin;
    private List<String> equippedItems;
    // ======================= ▲▲▲ 필드 추가 ▲▲▲ =======================
    private List<String> purchasedItems;
    private int characterLevel;
    
    public static UserProfileResponseDto fromEntity(User user) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setLoginId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setGender(user.getGender());
        dto.setBloodType(user.getBloodType());
        dto.setMbti(user.getMbti());
        // ======================= ▼▼▼ 로직 추가 ▼▼▼ =======================
        dto.setCoin(user.getCoin());
        dto.setEquippedItems(user.getEquippedItems());
        // ======================= ▲▲▲ 로직 추가 ▲▲▲ =======================
        dto.setPurchasedItems(user.getPurchasedItems());
        dto.setCharacterLevel(user.getCharacterLevel());
        return dto;
    }
}