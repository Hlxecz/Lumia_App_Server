package com.ch4.lumia_backend; // 메인 클래스와 같은 패키지

import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component // 이 클래스를 스프링 빈으로 등록하여 실행되도록 함
@RequiredArgsConstructor // 의존성 주입을 위한 생성자 자동 생성
public class TestDataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 애플리케이션 시작 시 자동으로 실행되는 메소드
    @Override
    public void run(String... args) throws Exception {
        System.out.println("===== TestDataInit: 테스트 사용자 데이터 생성 시작 =====");

        String testUserId = "testuser"; // 테스트용 아이디
        String testPassword = "password123"; // 테스트용 비밀번호 (암호화 전)

        // 이미 해당 아이디의 사용자가 있는지 확인 (중복 생성 방지)
        if (userRepository.findByUserId(testUserId).isEmpty()) {
            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(testPassword);

            // 테스트 User 객체 생성
            User testUser = User.builder()
                    .userId(testUserId)
                    .password(encodedPassword) // <<< 암호화된 비밀번호 저장!
                    .username("테스트유저")
                    .email("test@example.com") // 이메일 필드 추가됨
                    .role("ROLE_USER") // 기본 역할
                    .build();

            // UserRepository를 통해 DB에 저장
            userRepository.save(testUser);
            System.out.println(">>> 테스트 사용자(" + testUserId + ") 생성 완료.");

        } else {
            System.out.println(">>> 테스트 사용자(" + testUserId + ")는 이미 존재합니다.");
        }
        System.out.println("======================================================");
    }
}