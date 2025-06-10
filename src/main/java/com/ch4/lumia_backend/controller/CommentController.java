package com.ch4.lumia_backend.controller;

import com.ch4.lumia_backend.dto.CommentRequestDto;
import com.ch4.lumia_backend.dto.CommentResponseDto;
import com.ch4.lumia_backend.entity.Comment;
import com.ch4.lumia_backend.entity.Post;
import com.ch4.lumia_backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final String FASTAPI_URL = "http://3.143.210.229:8000/filter_post";
    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable(name = "postId") Long postId) {
        try {
            List<Comment> comments = commentService.getCommentsByPostId(postId);
            List<CommentResponseDto> response = comments.stream()
                    .map(CommentResponseDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("댓글 조회 실패 - postId: {}, error: {}", postId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 조회 실패");
        }
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<?> createComment(@PathVariable(name = "postId") Long postId,
                                           @RequestBody CommentRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            logger.warn("비인증 사용자 댓글 작성 시도 (postId: {}, currentUserId: {})", postId, currentUserId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자만 댓글 작성이 가능합니다.");
        }

        try {
            // FastAPI 필터링 호출
            Map<String, String> filterRequest = new HashMap<>();
            filterRequest.put("title", ""); // 비어있는 제목을 추가	
            filterRequest.put("content", dto.getContent());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(filterRequest, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(FASTAPI_URL, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && Boolean.TRUE.equals(body.get("blocked"))) {
                String reason = (String) body.get("reason");
                return ResponseEntity.badRequest().body(
                        Map.of("error", true, "message", "댓글 작성이 차단되었습니다.", "reason", reason != null ? reason : "금지된 내용")
                );
            }

            Comment comment = commentService.createComment(Post.fromId(postId), currentUserId, dto.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(new CommentResponseDto(comment));
        }  catch (IllegalArgumentException e) {
            logger.warn("댓글 작성 실패 (postId: {}, userId: {}): {}", postId, currentUserId, e.getMessage());
            if (e.getMessage().contains("게시글이 존재하지 않습니다")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("댓글 작성 실패 - userId: {}, error: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 작성 중 오류 발생");
        }
    }

    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable(name = "commentId") Long commentId,
                                           @RequestBody CommentRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            logger.warn("비인증 사용자 댓글 수정 시도 (commentId: {}, currentUserId: {})", commentId, currentUserId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // ======================= ▼▼▼ 필터링 로직 추가 ▼▼▼ =======================
            Map<String, String> filterRequest = new HashMap<>();
            filterRequest.put("title", "");
            filterRequest.put("content", dto.getContent());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(filterRequest, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(FASTAPI_URL, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && Boolean.TRUE.equals(body.get("blocked"))) {
                String reason = (String) body.get("reason");
                return ResponseEntity.badRequest().body(
                        Map.of("error", true, "message", "댓글 수정이 차단되었습니다.", "reason", reason != null ? reason : "금지된 내용")
                );
            }
            // ======================= ▲▲▲ 필터링 로직 추가 ▲▲▲ =======================

            Comment updated = commentService.updateComment(commentId, currentUserId, dto.getContent());
            return ResponseEntity.ok().body(new CommentResponseDto(updated));
        } catch (IllegalArgumentException e) {
            logger.warn("댓글 수정 실패 (commentId: {}, userId: {}): {}", commentId, currentUserId, e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            if (e.getMessage().contains("존재하지 않습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("댓글 수정 실패 - userId: {}, error: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 중 오류 발생");
        }
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable(name = "commentId") Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            logger.warn("비인증 사용자 댓글 삭제 시도 (commentId: {}, currentUserId: {})", commentId, currentUserId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            commentService.deleteComment(commentId, currentUserId);
            return ResponseEntity.ok().body("댓글이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            logger.warn("댓글 삭제 실패 (commentId: {}, userId: {}): {}", commentId, currentUserId, e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            if (e.getMessage().contains("존재하지 않습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("댓글 삭제 실패 - userId: {}, error: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 중 오류 발생");
        }
    }
}