// src/main/java/com/ch4/lumia_backend/entity/UserAnswer.java
package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_answers")
@Getter
@NoArgsConstructor
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Lob
    @Column(name = "answer_text", nullable = false)
    private String answerText;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "emotion_tag", length = 50)
    private String emotionTag;

    @PrePersist
    protected void onPersist() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }

    @Builder
    public UserAnswer(User user, Question question, String answerText, String emotionTag) {
        this.user = user;
        this.question = question;
        this.answerText = answerText;
        this.emotionTag = emotionTag;
    }
}