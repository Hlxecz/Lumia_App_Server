package com.ch4.lumia_backend.controller;

import com.ch4.lumia_backend.dto.PostRequestDto;
import com.ch4.lumia_backend.dto.PostResponseDto;
import com.ch4.lumia_backend.entity.Post;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.service.ContentFilterService;
import com.ch4.lumia_backend.service.PostService;
import com.ch4.lumia_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;
    private final UserService userService;
    private final ContentFilterService contentFilterService;

    @GetMapping("/list")
    public ResponseEntity<?> getPosts(@RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "5") int size) {
        logger.info("게시글 목록 조회 요청 - page: {}, size: {}", page, size);
        try {
            Page<Post> postPage = postService.getPosts(page, size);
            Page<PostResponseDto> responsePage = postPage.map(PostResponseDto::new);
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            logger.error("게시글 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", "게시글 조회 중 오류 발생"));
        }
    }

    @PostMapping("/write")
    public ResponseEntity<?> createPost(@RequestBody PostRequestDto postDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", true, "message", "인증 정보가 없습니다."));
        }

        try {
            Map<String, Object> filterResult = filterPostContent(postDto);
            if (filterResult != null && Boolean.TRUE.equals(filterResult.get("blocked"))) {
                String reason = (String) filterResult.get("reason");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "게시글 작성이 차단되었습니다.",
                        "reason", reason != null ? reason : "금지된 내용"
                ));
            }

            User user = userService.findByUserId(currentUserId);
            Post createdPost = postService.createPost(
                    postDto.getCategory(),
                    postDto.getTitle(),
                    postDto.getContent(),
                    user
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new PostResponseDto(createdPost));
        } catch (IllegalStateException e) {
            logger.warn("Content filter unavailable for user {}: {}", currentUserId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", true, "message", "게시글 검증 서버에 연결할 수 없습니다."));
        } catch (Exception e) {
            logger.error("게시글 작성 실패 - {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", "게시글 작성 중 오류 발생"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostDetail(@PathVariable(name = "id") Long id) {
        try {
            Post post = postService.getPostById(id);
            return ResponseEntity.ok(new PostResponseDto(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("게시글 상세 조회 실패 - {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", "게시글 조회 중 오류 발생"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable(name = "id") Long id, @RequestBody PostRequestDto postDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", true, "message", "인증 정보가 없습니다."));
        }

        try {
            Map<String, Object> filterResult = filterPostContent(postDto);
            if (filterResult != null && Boolean.TRUE.equals(filterResult.get("blocked"))) {
                String reason = (String) filterResult.get("reason");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "게시글 수정이 차단되었습니다.",
                        "reason", reason != null ? reason : "금지된 내용"
                ));
            }

            User user = userService.findByUserId(currentUserId);
            Post updatedPost = postService.updatePost(id, postDto, user);
            return ResponseEntity.ok(new PostResponseDto(updatedPost));
        } catch (IllegalStateException e) {
            logger.warn("Content filter unavailable for update {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", true, "message", "게시글 검증 서버에 연결할 수 없습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("게시글 수정 실패 - {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", "게시글 수정 중 오류 발생"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable(name = "id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        if (currentUserId == null || "anonymousUser".equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", true, "message", "인증 정보가 없습니다."));
        }

        try {
            User user = userService.findByUserId(currentUserId);
            postService.deletePost(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("게시글 삭제 실패 - {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", "게시글 삭제 중 오류 발생"));
        }
    }

    private Map<String, Object> filterPostContent(PostRequestDto postDto) {
        return contentFilterService.filterContent(postDto.getTitle(), postDto.getContent());
    }
}
