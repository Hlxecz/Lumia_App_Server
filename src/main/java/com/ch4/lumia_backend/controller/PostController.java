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
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
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
    private final RestTemplate restTemplate = new RestTemplate();
    private final String FASTAPI_URL = "http://3.143.210.229:8000/filter_post"; // EC2라면 외부 IP로 변경

    @GetMapping("/list")
    public ResponseEntity<?> getPosts(@RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size",defaultValue = "5") int size) {
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
            // FastAPI 필터링 호출
            Map<String, String> filterRequest = new HashMap<>();
            filterRequest.put("title", postDto.getTitle());
            filterRequest.put("content", postDto.getContent());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(filterRequest, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(FASTAPI_URL, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && Boolean.TRUE.equals(body.get("blocked"))) {
                String reason = (String) body.get("reason");
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "error", true,
                                "message", "게시글 작성이 차단되었습니다.",
                                "reason", reason != null ? reason : "금지된 내용"
                        )
                );
            }

            User user = userService.findByUserId(currentUserId);
            Post createdPost = postService.createPost(
                    postDto.getCategory(),
                    postDto.getTitle(),
                    postDto.getContent(),
                    user
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new PostResponseDto(createdPost));

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
            // 욕설/혐오 필터링 재검사
            Map<String, String> filterRequest = new HashMap<>();
            filterRequest.put("title", postDto.getTitle());
            filterRequest.put("content", postDto.getContent());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(filterRequest, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(FASTAPI_URL, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && Boolean.TRUE.equals(body.get("blocked"))) {
                String reason = (String) body.get("reason");
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "error", true,
                                "message", "게시글 수정이 차단되었습니다.",
                                "reason", reason != null ? reason : "금지된 내용"
                        )
                );
            }

            User user = userService.findByUserId(currentUserId);
            Post updatedPost = postService.updatePost(id, postDto, user);
            return ResponseEntity.ok(new PostResponseDto(updatedPost));

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
}
