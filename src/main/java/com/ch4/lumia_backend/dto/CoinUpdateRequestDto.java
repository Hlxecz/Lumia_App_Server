package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CoinUpdateRequestDto {
    private int amount; // 변경할 코인 양 (음수일 경우 차감)
}