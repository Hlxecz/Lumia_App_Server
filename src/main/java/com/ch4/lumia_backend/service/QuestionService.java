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
    private final UserAnswerRepository userAnswerRepository;

    @Transactional
    public NewMessageResponseDto getScheduledQuestionForUser(String userId) {
        User user = findUserByLoginId(userId);
        UserSetting setting = findOrCreateUserSetting(user);
        LocalDateTime lastIssuedAt = setting.getLastIssuedAt();

        // 1. 최초 사용자(질문 받은 기록 없음)인 경우, 시간과 관계없이 즉시 첫 질문 제공
        if (lastIssuedAt == null) {
            logger.info("First question for new user {}. Providing immediately.", userId);
            return issueNewQuestion(setting, "SCHEDULED_MESSAGE", true);
        }

        // 2. 기존 사용자의 경우, 답변 안 한 메시지가 있는지 확인
        if (setting.getLastIssuedQuestionId() != null) {
            Question lastQuestion = questionRepository.findById(setting.getLastIssuedQuestionId()).orElse(null);
            if (lastQuestion != null && !userAnswerRepository.existsByUserAndQuestion(user, lastQuestion)) {
                logger.info("User {} has an unanswered question (ID: {}). Returning it.", userId, lastQuestion.getId());
                return new NewMessageResponseDto(false, QuestionDto.fromEntity(lastQuestion));
            }
        }
        
        // 3. 답변도 완료했고, 이제 다음 정기 질문을 받을 시간인지 확인
        if (setting.getNotificationTime() == null) {
            return new NewMessageResponseDto(false, null);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayScheduledTime = now.toLocalDate().atTime(setting.getNotificationTime());

        // ======================= ▼▼▼ 수정된 부분 ▼▼▼ =======================
        // 마지막 질문 받은 날짜가 '오늘'보다 이전인지 확인하는 것으로 조건 변경
        if (now.isAfter(todayScheduledTime) && lastIssuedAt.toLocalDate().isBefore(now.toLocalDate())) {
            return issueNewQuestion(setting, "SCHEDULED_MESSAGE", true);
        }
        // ======================= ▲▲▲ 수정된 부분 ▲▲▲ =======================

        logger.debug("Not yet time for a new message for user {}", userId);
        return new NewMessageResponseDto(false, null);
    }

    @Transactional
    public NewMessageResponseDto getOnDemandQuestion(String userId) {
        User user = findUserByLoginId(userId);
        UserSetting setting = findOrCreateUserSetting(user);

        if (setting.getLastDailyMoodAt() != null && setting.getLastDailyMoodAt().toLocalDate().isEqual(LocalDate.now())) {
            logger.warn("User {} requested an on-demand question today already.", userId);
            throw new IllegalStateException("추가 질문은 하루에 한 번만 받을 수 있어요.");
        }
        
        setting.setLastDailyMoodAt(LocalDateTime.now());
        return issueNewQuestion(setting, "DAILY_MOOD", false);
    }

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
                        .notificationTime(LocalTime.of(13,0))
                        .build();
                    return userSettingRepository.save(defaultSettings);
                });
    }
}