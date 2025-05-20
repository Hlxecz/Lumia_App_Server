// src/main/java/com/ch4/lumia_backend/service/QuestionService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.NewMessageResponseDto;
import com.ch4.lumia_backend.dto.QuestionDto;
import com.ch4.lumia_backend.entity.Question;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserSetting;
import com.ch4.lumia_backend.repository.QuestionRepository;
import com.ch4.lumia_backend.repository.UserRepository;
import com.ch4.lumia_backend.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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

    // 이 서비스의 메서드는 사용자의 설정을 읽고, 필요하다면 lastScheduledMessageAt을 업데이트하므로 @Transactional 추가
    @Transactional
    public NewMessageResponseDto getQuestionForUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found for ID: {}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
                });

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElseGet(() -> { // 설정이 없으면 기본값 생성 (방어 로직)
                    logger.info("UserSetting not found for user {}, creating default settings.", userId);
                    UserSetting defaultSettings = UserSetting.builder()
                            .user(user)
                            .notificationInterval("WHEN_APP_OPENS") // 기본값
                            .inAppNotificationEnabled(true)
                            .pushNotificationEnabled(true)
                            .build();
                    return userSettingRepository.save(defaultSettings);
                });

        if (!setting.isInAppNotificationEnabled()) {
            logger.debug("In-app notifications disabled for user {}. No message will be provided.", userId);
            return new NewMessageResponseDto(false, null);
        }

        boolean shouldProvideMessage = false;
        LocalDateTime now = LocalDateTime.now();
        String interval = setting.getNotificationInterval();
        LocalDateTime lastMessageTime = setting.getLastScheduledMessageAt();

        logger.debug("Checking for new message for user {}. Interval: {}, LastMsgTime: {}", userId, interval, lastMessageTime);


        switch (interval) {
            case "WHEN_APP_OPENS":
                // 앱 열 때마다 새 메시지 (단, 최소 간격(예: 3시간)을 두어 너무 자주 주지 않도록)
                if (lastMessageTime == null || Duration.between(lastMessageTime, now).toHours() >= 3) {
                    shouldProvideMessage = true;
                    logger.debug("WHEN_APP_OPENS: Condition met for user {}", userId);
                }
                break;
            case "DAILY_SPECIFIC_TIME":
                LocalTime notificationTime = setting.getNotificationTime();
                if (notificationTime != null) {
                    LocalDateTime scheduledTimeToday = now.toLocalDate().atTime(notificationTime);
                    // 오늘 예정된 시간 이후이고, 마지막 메시지가 오늘 이전이거나 오늘 예정된 시간 이전인 경우
                    if (now.isAfter(scheduledTimeToday) &&
                        (lastMessageTime == null ||
                         lastMessageTime.isBefore(scheduledTimeToday))) {
                        shouldProvideMessage = true;
                        logger.debug("DAILY_SPECIFIC_TIME: Condition met for user {} at {}", userId, notificationTime);
                    }
                }
                break;
            case "NONE":
            default:
                shouldProvideMessage = false;
                logger.debug("Interval is NONE or default for user {}. No scheduled message.", userId);
                break;
        }

        if (shouldProvideMessage) {
            // TODO: 사용자가 최근에 답변한 질문, 질문 풀 관리 로직 추가
            Optional<Question> questionOpt = questionRepository.findRandomActiveQuestionByType("SCHEDULED_MESSAGE");
            if (questionOpt.isPresent()) {
                logger.info("Providing new scheduled message (ID: {}) to user {}", questionOpt.get().getId(), userId);
                setting.setLastScheduledMessageAt(now); // 메시지 제공 시간 업데이트
                // userSettingRepository.save(setting); // 엔티티가 dirty checking에 의해 자동 업데이트됨 (Transactional)
                return new NewMessageResponseDto(true, QuestionDto.fromEntity(questionOpt.get()));
            } else {
                logger.warn("No active 'SCHEDULED_MESSAGE' type questions found.");
            }
        }

        // 스케줄된 메시지가 없거나 줄 시간이 아닐 때, 일반 질문 제공 (선택적)
        // logger.debug("No scheduled message for user {}. Checking for a general daily question.", userId);
        // Optional<Question> dailyQuestionOpt = questionRepository.findRandomActiveQuestionByType("DAILY_MOOD");
        // if (dailyQuestionOpt.isPresent()) {
        //     logger.info("Providing general daily mood question (ID: {}) to user {}", dailyQuestionOpt.get().getId(), userId);
        //     return new NewMessageResponseDto(false, QuestionDto.fromEntity(dailyQuestionOpt.get())); // hasNewMessage는 false
        // }

        logger.debug("No message to provide to user {} at this time.", userId);
        return new NewMessageResponseDto(false, null);
    }
}