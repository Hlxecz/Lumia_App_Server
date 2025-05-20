// src/main/java/com/ch4/lumia_backend/dto/NewMessageResponseDto.java
package com.ch4.lumia_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewMessageResponseDto {
    private boolean hasNewMessage;
    private QuestionDto newMessage; // 새로운 메시지가 있을 경우, 해당 메시지(질문) 정보
}