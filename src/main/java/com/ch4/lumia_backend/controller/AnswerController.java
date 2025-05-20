// src/main/java/com/ch4/lumia_backend/controller/AnswerController.java
package com.ch4.lumia_backend.controller;

import com.ch4.lumia_backend.dto.AnswerRequestDto;
import com.ch4.lumia_backend.dto.AnswerResponseDto;
import com.ch4.lumia_backend.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private static final Logger logger = LoggerFactory.getLogger(AnswerController.class);
    private final AnswerService answerService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    @PostMapping
    public ResponseEntity<?> saveAnswer(@RequestBody AnswerRequestDto answerRequestDto) {
        String userId = getCurrentUserId();
        if (userId == null) {
            logger.warn("Attempt to save answer without authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }
        logger.info("Saving answer for user: {} to questionId: {}", userId, answerRequestDto.getQuestionId());
        try {
            AnswerResponseDto savedAnswer = answerService.saveAnswer(answerRequestDto, userId);
            logger.info("Answer saved successfully for user: {}, answerId: {}", userId, savedAnswer.getAnswerId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAnswer);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to save answer for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error saving answer for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("답변 저장 중 오류 발생");
        }
    }

    @GetMapping("/my-records")
    public ResponseEntity<?> getMyRecords(@PageableDefault(size = 10, sort = "answeredAt") Pageable pageable) {
        String userId = getCurrentUserId();
        if (userId == null) {
            logger.warn("Attempt to get records without authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }
        logger.info("Fetching records for user: {}, page: {}, size: {}", userId, pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<AnswerResponseDto> myRecords = answerService.getMyAnswers(userId, pageable);
            return ResponseEntity.ok(myRecords);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get records for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching records for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("기록 조회 중 오류 발생");
        }
    }
}