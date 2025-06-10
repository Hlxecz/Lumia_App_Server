// src/main/java/com/ch4/lumia_backend/dto/AnswerRequestDto.java
package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // Controller에서 @RequestBody로 받기 위해 필요
public class AnswerRequestDto {
    private Long questionId;
    private String content;
    private String emotionTag; // 선택 사항	
}