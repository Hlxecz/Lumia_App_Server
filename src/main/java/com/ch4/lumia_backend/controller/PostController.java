package com.ch4.lumia_backend.controller;

import com.ch4.lumia_backend.dto.PostRequestDto;
import com.ch4.lumia_backend.dto.PostResponseDto;
import com.ch4.lumia_backend.entity.Post;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.service.PostService;
import com.ch4.lumia_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;
    private final UserService userService;

    @GetMapping("/list")
    public ResponseEntity<?> getPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size
    ) {
        logger.info("게시글 목록 조회 요청 - page: {}, size: {}", page, size);
        try {
            Page<Post> postPage = postService.getPosts(page, size);
            Page<PostResponseDto> responsePage = postPage.map(PostResponseDto::new);
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            logger.error("게시글 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 조회 중 오류 발생");
        }
    }

    @PostMapping("/write")
    public ResponseEntity<?> createPost(@RequestBody PostRequestDto postDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            logger.warn("비인증 사용자 게시글 작성 시도 (currentUserId: {})", currentUserId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다. 로그인이 필요합니다.");
        }

        try {
            logger.info("게시글 작성 요청 - 작성자: {}", currentUserId);
            User user = userService.findByUserId(currentUserId);

            Post createdPost = postService.createPost(
                    postDto.getCategory(),
                    postDto.getTitle(),
                    postDto.getContent(),
                    user
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new PostResponseDto(createdPost));

        } catch (IllegalArgumentException e) { 
            logger.warn("게시글 작성 실패 (사용자 조회 실패 또는 서비스 로직 오류) - {}: {}", currentUserId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("게시글 작성 실패 - {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 작성 중 오류 발생");
        }
    }

    /**
     * 게시글 상세 조회 - GET /api/posts/{id}
     * SecurityConfig에서 permitAll()로 설정됨.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostDetail(@PathVariable(name = "id") Long id) { // "id" 이름 명시
        try {
            Post post = postService.getPostById(id);
            return ResponseEntity.ok(new PostResponseDto(post));
        } catch (IllegalArgumentException e) { 
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("게시글 상세 조회 실패 - {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 조회 중 오류 발생");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable(name = "id") Long id, @RequestBody PostRequestDto postDto) { // "id" 이름 명시
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            logger.warn("비인증 사용자 게시글 수정 시도 (postId: {}, currentUserId: {})", id, currentUserId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다. 로그인이 필요합니다.");
        }

        try {
            User user = userService.findByUserId(currentUserId);
            Post updatedPost = postService.updatePost(id, postDto, user);
            return ResponseEntity.ok(new PostResponseDto(updatedPost));
        } catch (IllegalArgumentException e) { 
            logger.warn("게시글 수정 실패 (postId: {}, userId: {}): {}", id, currentUserId, e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("게시글 수정 실패 - (postId: {}, userId: {}): {}", id, currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 수정 중 오류 발생");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable(name = "id") Long id) { // "id" 이름 명시
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            logger.warn("비인증 사용자 게시글 삭제 시도 (postId: {}, currentUserId: {})", id, currentUserId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다. 로그인이 필요합니다.");
        }

        try {
            User user = userService.findByUserId(currentUserId);
            postService.deletePost(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) { 
            logger.warn("게시글 삭제 실패 (postId: {}, userId: {}): {}", id, currentUserId, e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("게시글 삭제 실패 - (postId: {}, userId: {}): {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 삭제 중 오류 발생");
        }
    }
}