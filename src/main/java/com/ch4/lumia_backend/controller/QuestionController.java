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

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }
    
    // 정기 질문
    @GetMapping("/for-me")
    public ResponseEntity<?> getQuestionForCurrentUser() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }

        try {
            NewMessageResponseDto responseDto = questionService.getScheduledQuestionForUser(currentUserId);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            logger.error("Error fetching scheduled question for user {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("질문 조회 중 오류 발생");
        }
    }

    // ======================= ▼▼▼ 엔드포인트 추가 ▼▼▼ =======================
    // 추가 질문 (On-Demand)
    @GetMapping("/on-demand")
    public ResponseEntity<?> getOnDemandQuestion() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        }

        try {
            NewMessageResponseDto responseDto = questionService.getOnDemandQuestion(currentUserId);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalStateException e) {
            // 하루 한 번 제한에 걸렸을 때
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching on-demand question for user {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("추가 질문 조회 중 오류 발생");
        }
    }
    // ======================= ▲▲▲ 엔드포인트 추가 ▲▲▲ =======================
}