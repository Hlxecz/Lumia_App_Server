// src/main/java/com/ch4/lumia_backend/controller/QuestionController.java
package com.ch4.lumia_backend.controller;

import com.ch4.lumia_backend.dto.NewMessageResponseDto;
import com.ch4.lumia_backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);
    private final QuestionService questionService;

    @GetMapping("/for-me")
    public ResponseEntity<?> getQuestionForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // ▼▼▼ --- 수정된 부분 --- ▼▼▼
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            logger.warn("getQuestionForCurrentUser: Authentication is null or user is anonymous. Responding with 401. Path: /api/questions/for-me");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("인증 정보가 유효하지 않거나 만료되었습니다. 토큰 재발급이 필요합니다.");
        }
        // ▲▲▲ --- 수정된 부분 --- ▲▲▲

        String currentUserId = authentication.getName(); // 이제 authentication 객체는 null이 아님이 보장됨

        if (currentUserId == null ) { // 위에서 anonymousUser도 걸렀으므로, 이 조건은 사실상 도달하기 어려움
            logger.warn("Attempt to get question for-me without proper user ID after authentication check.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보 내 사용자 ID가 없습니다.");
        }

        logger.info("Fetching question for-me for user: {}", currentUserId);
        try {
            NewMessageResponseDto responseDto = questionService.getQuestionForUser(currentUserId);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get question for-me for user {}: {}", currentUserId, e.getMessage());
            // 사용자를 찾을 수 없는 경우는 404가 더 적절할 수 있음
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching question for-me for user {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("질문 조회 중 오류 발생");
        }
    }
}