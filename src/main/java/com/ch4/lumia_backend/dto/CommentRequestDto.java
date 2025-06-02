// src/main/java/com/ch4/lumia_backend/dto/CommentRequestDto.java
package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Setter 추가

@Getter
@Setter // JSON 바인딩을 위해 Setter 추가
@NoArgsConstructor
public class CommentRequestDto {
    private String content;
}