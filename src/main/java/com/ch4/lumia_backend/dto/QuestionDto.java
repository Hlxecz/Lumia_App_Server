// src/main/java/com/ch4/lumia_backend/dto/QuestionDto.java
package com.ch4.lumia_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long questionId;
    private String questionText;
    private String questionType;

    // 정적 팩토리 메소드 (Entity -> DTO 변환)
    public static QuestionDto fromEntity(com.ch4.lumia_backend.entity.Question question) {
        if (question == null) return null;
        return new QuestionDto(question.getId(), question.getQuestionText(), question.getQuestionType());
    }
}