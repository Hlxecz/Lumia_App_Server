// src/main/java/com/ch4/lumia_backend/service/AnswerService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.AnswerRequestDto;
import com.ch4.lumia_backend.dto.AnswerResponseDto; // DTO import
import com.ch4.lumia_backend.entity.Question;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserAnswer;
import com.ch4.lumia_backend.repository.QuestionRepository;
import com.ch4.lumia_backend.repository.UserAnswerRepository;
import com.ch4.lumia_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public AnswerResponseDto saveAnswer(AnswerRequestDto answerRequestDto, String userLoginId) {
        User user = userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userLoginId));
        Question question = questionRepository.findById(answerRequestDto.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다: " + answerRequestDto.getQuestionId()));

        UserAnswer userAnswer = UserAnswer.builder()
                .user(user)
                .question(question)
                .answerText(answerRequestDto.getAnswerText())
                .emotionTag(answerRequestDto.getEmotionTag())
                .build();
        // answeredAt은 @PrePersist로 UserAnswer 엔티티 내에서 자동 설정됨

        UserAnswer savedAnswer = userAnswerRepository.save(userAnswer);

        // DTO의 정적 팩토리 메소드를 사용하여 객체 생성
        return AnswerResponseDto.fromEntity(savedAnswer);
    }

    @Transactional(readOnly = true)
    public Page<AnswerResponseDto> getMyAnswers(String userLoginId, Pageable pageable) {
        User user = userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userLoginId));

        Page<UserAnswer> answerPage = userAnswerRepository.findByUserOrderByAnsweredAtDesc(user, pageable);

        // Page 객체의 map 기능을 사용하여 각 UserAnswer 엔티티를 AnswerResponseDto로 변환
        return answerPage.map(AnswerResponseDto::fromEntity);
    }
}