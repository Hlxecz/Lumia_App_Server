// src/main/java/com/ch4/lumia_backend/dto/AnswerResponseDto.java
package com.ch4.lumia_backend.dto;

import com.ch4.lumia_backend.entity.UserAnswer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor // 이 어노테이션이 필드 순서대로 생성자를 만듭니다.
public class AnswerResponseDto {
    private Long answerId;     // 1번째
    private Long questionId;   // 2번째
    private String questionText; // 3번째 - 원본 질문 텍스트
    private String answerText;   // 4번째
    private String emotionTag;   // 5번째
    private LocalDateTime answeredAt; // 6번째

    // 정적 팩토리 메소드 (Entity -> DTO 변환)
    public static AnswerResponseDto fromEntity(UserAnswer userAnswer) {
        if (userAnswer == null) return null;
        return new AnswerResponseDto(
            userAnswer.getId(),                      // answerId
            userAnswer.getQuestion().getId(),        // questionId
            userAnswer.getQuestion().getQuestionText(),// questionText
            userAnswer.getAnswerText(),              // answerText
            userAnswer.getEmotionTag(),              // emotionTag
            userAnswer.getAnsweredAt()               // answeredAt
        );
    }
}