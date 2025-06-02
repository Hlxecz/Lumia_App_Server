package com.ch4.lumia_backend.controller;

import com.ch4.lumia_backend.dto.CommentRequestDto;
import com.ch4.lumia_backend.dto.CommentResponseDto;
import com.ch4.lumia_backend.entity.Comment;
import com.ch4.lumia_backend.entity.Post;
import com.ch4.lumia_backend.service.CommentService;
import com.ch4.lumia_backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;
    private final PostService postService;

    /**
     * 댓글 조회 - GET /api/posts/{postId}/comments
     */
    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable("id") Long postId) {
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

    /**
     * 댓글 작성 - POST /api/posts/{postId}/comments
     */
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<?> createComment(@PathVariable("id") Long postId,
                                           @RequestBody CommentRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자만 댓글 작성이 가능합니다.");
        }

        try {
            Post post = postService.getPostById(postId);
            Comment comment = commentService.createComment(post, currentUserId, dto.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(new CommentResponseDto(comment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("댓글 작성 실패 - userId: {}, error: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 작성 중 오류 발생");
        }
    }

    /**
     * 댓글 수정 - PUT /api/comments/{commentId}
     */
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestBody CommentRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            Comment updated = commentService.updateComment(commentId, currentUserId, dto.getContent());
            return ResponseEntity.ok().body(new CommentResponseDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("댓글 수정 실패 - userId: {}, error: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 중 오류 발생");
        }
    }

    /**
     * 댓글 삭제 - DELETE /api/comments/{commentId}
     */
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            commentService.deleteComment(commentId, currentUserId);
            return ResponseEntity.ok().body("댓글이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("댓글 삭제 실패 - userId: {}, error: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 중 오류 발생");
        }
    }
}
