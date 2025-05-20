// src/main/java/com/ch4/lumia_backend/entity/Question.java
package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor // JPA는 기본 생성자를 필요로 함
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Lob // TEXT 타입 매핑
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "question_type", length = 50)
    private String questionType; // 예: "DAILY_MOOD", "SCHEDULED_MESSAGE"

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    @Builder
    public Question(String questionText, String questionType, boolean isActive) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.isActive = isActive;
    }
}