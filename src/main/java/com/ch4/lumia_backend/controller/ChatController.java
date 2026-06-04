package com.ch4.lumia_backend.controller;

import com.ch4.lumia_backend.dto.ChatCompletionRequestDto;
import com.ch4.lumia_backend.dto.ChatCompletionResponseDto;
import com.ch4.lumia_backend.exception.ChatCompletionException;
import com.ch4.lumia_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    @PostMapping("/completions")
    public ResponseEntity<?> createCompletion(@RequestBody ChatCompletionRequestDto requestDto) {
        String currentUserId = getCurrentUserId();
        String requestedBy = StringUtils.hasText(currentUserId) ? currentUserId : "anonymous";

        if (requestDto == null || !StringUtils.hasText(requestDto.getMessage())) {
            logger.warn("Empty chat message received from user: {}", requestedBy);
            return ResponseEntity.badRequest().body("message는 비어 있을 수 없습니다.");
        }

        logger.info("Chat completion requested by user: {}", requestedBy);

        try {
            ChatCompletionResponseDto responseDto = chatService.createCompletion(requestDto.getMessage(), currentUserId);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid chat request from user {}: {}", requestedBy, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ChatCompletionException e) {
            logger.error("Chat completion failed for user {}: {}", requestedBy, e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during chat completion for user {}: {}", requestedBy, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("채팅 응답 생성 중 오류가 발생했습니다.");
        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }
}
