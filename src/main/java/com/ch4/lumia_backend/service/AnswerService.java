// src/main/java/com/ch4/lumia_backend/service/AnswerService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.AnswerRequestDto;
import com.ch4.lumia_backend.dto.AnswerResponseDto;
import com.ch4.lumia_backend.entity.Question;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserAnswer;
import com.ch4.lumia_backend.repository.QuestionRepository;
import com.ch4.lumia_backend.repository.UserAnswerRepository;
import com.ch4.lumia_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnswerService.class);
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
                .answerText(answerRequestDto.getContent())
                .emotionTag(answerRequestDto.getEmotionTag())
                .build();
        
        userAnswerRepository.save(userAnswer);

        // 답변 1개당 코인 1개 지급
        user.setCoin(user.getCoin() + 1);
        logger.info("Awarded 1 coin to user {}. Total coins: {}", user.getUserId(), user.getCoin());

        // ======================= ▼▼▼ 로직 추가 ▼▼▼ =======================
        // 답변 시 캐릭터 레벨업 로직
        if (user.getCharacterLevel() < 3) {
            user.setCharacterLevel(user.getCharacterLevel() + 1);
            logger.info("Character level up for user {}. New level: {}", user.getUserId(), user.getCharacterLevel());
        }
        // ======================= ▲▲▲ 로직 추가 ▲▲▲ =======================

        UserAnswer finalAnswerState = userAnswerRepository.findById(userAnswer.getId()).get();
        return AnswerResponseDto.fromEntity(finalAnswerState);
    }

    @Transactional(readOnly = true)
    public Page<AnswerResponseDto> getMyAnswers(String userLoginId, Pageable pageable) {
        User user = userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userLoginId));
        Page<UserAnswer> answerPage = userAnswerRepository.findByUserOrderByAnsweredAtDesc(user, pageable);
        return answerPage.map(AnswerResponseDto::fromEntity);
    }
}