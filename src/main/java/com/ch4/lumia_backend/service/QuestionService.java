// src/main/java/com/ch4/lumia_backend/service/QuestionService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.NewMessageResponseDto;
import com.ch4.lumia_backend.dto.QuestionDto;
import com.ch4.lumia_backend.entity.Question;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserSetting;
import com.ch4.lumia_backend.repository.QuestionRepository;
import com.ch4.lumia_backend.repository.UserAnswerRepository;
import com.ch4.lumia_backend.repository.UserRepository;
import com.ch4.lumia_backend.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserAnswerRepository userAnswerRepository; // 답변 확인용

    // [이름 변경] 정기 질문을 가져오는 메소드
    @Transactional
    public NewMessageResponseDto getScheduledQuestionForUser(String userId) {
        User user = findUserByLoginId(userId);
        UserSetting setting = findOrCreateUserSetting(user);

        // 5번 요구사항: 답변 안 한 메시지 확인
        if (setting.getLastIssuedQuestionId() != null) {
            Question lastQuestion = questionRepository.findById(setting.getLastIssuedQuestionId()).orElse(null);
            if (lastQuestion != null && !userAnswerRepository.existsByUserAndQuestion(user, lastQuestion)) {
                logger.info("User {} has an unanswered question (ID: {}). Returning it.", userId, lastQuestion.getId());
                return new NewMessageResponseDto(false, QuestionDto.fromEntity(lastQuestion));
            }
        }
        
        // 3번 & 5번 요구사항: 새로운 정기 질문 제공 시간 확인
        if (setting.getNotificationTime() == null) {
            return new NewMessageResponseDto(false, null); // 시간 설정 안했으면 제공 안 함
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayScheduledTime = now.toLocalDate().atTime(setting.getNotificationTime());
        LocalDateTime lastIssuedAt = setting.getLastIssuedAt();

        if (now.isAfter(todayScheduledTime) && (lastIssuedAt == null || lastIssuedAt.isBefore(todayScheduledTime))) {
            return issueNewQuestion(setting, "SCHEDULED_MESSAGE", true);
        }

        return new NewMessageResponseDto(false, null);
    }

    // 4번 요구사항: 추가 질문(On-Demand)을 가져오는 메소드
    @Transactional
    public NewMessageResponseDto getOnDemandQuestion(String userId) {
        User user = findUserByLoginId(userId);
        UserSetting setting = findOrCreateUserSetting(user);

        // 하루에 한 번만 제공하는 규칙 확인
        if (setting.getLastDailyMoodAt() != null && setting.getLastDailyMoodAt().toLocalDate().isEqual(LocalDate.now())) {
            logger.warn("User {} requested an on-demand question today already.", userId);
            throw new IllegalStateException("추가 질문은 하루에 한 번만 받을 수 있어요.");
        }
        
        setting.setLastDailyMoodAt(LocalDateTime.now());
        return issueNewQuestion(setting, "DAILY_MOOD", false);
    }

    // 공통 로직: 새로운 질문을 찾아서 제공하고 상태를 업데이트
    private NewMessageResponseDto issueNewQuestion(UserSetting setting, String questionType, boolean isScheduled) {
        Optional<Question> questionOpt = questionRepository.findRandomActiveQuestionByType(questionType);

        if (questionOpt.isPresent()) {
            Question newQuestion = questionOpt.get();
            setting.setLastIssuedAt(LocalDateTime.now());
            setting.setLastIssuedQuestionId(newQuestion.getId());
            
            logger.info("Issuing new {} question (ID: {}) to user {}", 
                        questionType, newQuestion.getId(), setting.getUser().getUserId());
            
            return new NewMessageResponseDto(isScheduled, QuestionDto.fromEntity(newQuestion));
        } else {
            logger.warn("No active '{}' type questions found in the database.", questionType);
            return new NewMessageResponseDto(false, null);
        }
    }

    private User findUserByLoginId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }
    
    private UserSetting findOrCreateUserSetting(User user) {
        return userSettingRepository.findByUser(user)
                .orElseGet(() -> {
                    logger.info("UserSetting not found for user {}, creating default settings.", user.getUserId());
                    UserSetting defaultSettings = UserSetting.builder()
                        .user(user)
                        .notificationTime(LocalTime.of(13,0)) // 기본 시간 설정
                        .build();
                    return userSettingRepository.save(defaultSettings);
                });
    }
}