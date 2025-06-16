package com.ch4.lumia_backend;

import com.ch4.lumia_backend.entity.Question;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.repository.QuestionRepository;
import com.ch4.lumia_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestionRepository questionRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("===== TestDataInit: 테스트 데이터 생성 시작 =====");

        // --- 테스트 사용자 데이터 생성 ---
        String testUserId = "testuser";
        String testPassword = "password123";

        if (userRepository.findByUserId(testUserId).isEmpty()) {
            String encodedPassword = passwordEncoder.encode(testPassword);
            User testUser = User.builder()
                    .userId(testUserId)
                    .password(encodedPassword)
                    .username("테스트유저")
                    .email("test@example.com")
                    .role("ROLE_USER")
                    .coin(0)
                    .characterLevel(1)
                    .build();
            userRepository.save(testUser);
            System.out.println(">>> 테스트 사용자(" + testUserId + ") 생성 완료.");
        } else {
            System.out.println(">>> 테스트 사용자(" + testUserId + ")는 이미 존재합니다.");
        }

        // --- 기본 질문 데이터 생성 ---
        if (questionRepository.count() == 0) {
            System.out.println(">>> 기본 질문 데이터 생성 시작...");
            List<Question> initialQuestions = Arrays.asList(
                Question.builder()
                    .questionText("오늘 하루, 당신을 웃게 만든 작은 순간은 무엇이었나요?")
                    .questionType("SCHEDULED_MESSAGE")
                    .isActive(true)
                    .build(),
                Question.builder()
                    .questionText("지금 가장 감사하게 생각나는 사람이나 일이 있다면 무엇인가요?")
                    .questionType("SCHEDULED_MESSAGE")
                    .isActive(true)
                    .build(),
                Question.builder()
                    .questionText("요즘 당신의 마음을 가장 편안하게 해주는 것은 무엇인가요?")
                    .questionType("SCHEDULED_MESSAGE")
                    .isActive(true)
                    .build(),
                Question.builder()
                    .questionText("새롭게 도전해보고 싶은 것이 있나요? 있다면 무엇이고, 이유는 무엇인가요?")
                    .questionType("SCHEDULED_MESSAGE")
                    .isActive(true)
                    .build(),
                Question.builder()
                    .questionText("오늘 나에게 가장 필요했던 위로는 무엇이었을까요?")
                    .questionType("SCHEDULED_MESSAGE")
                    .isActive(true)
                    .build(),
                Question.builder()
                    .questionText("오늘 하루 중 가장 평화로웠던 순간은 언제였나요?")
                    .questionType("DAILY_MOOD")
                    .isActive(true)
                    .build(),
                Question.builder()
                    .questionText("지금 당신의 기분을 색깔로 표현한다면 어떤 색일까요?")
                    .questionType("DAILY_MOOD")
                    .isActive(true)
                    .build()
            );

            questionRepository.saveAll(initialQuestions);
            System.out.println(">>> 기본 질문 " + initialQuestions.size() + "개 생성 완료.");
        } else {
            System.out.println(">>> 질문 데이터가 이미 존재합니다. (기본 질문 생성 건너뜀)");
        }

        System.out.println("======================================================");
    }
}