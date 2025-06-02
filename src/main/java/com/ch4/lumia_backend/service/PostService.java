package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.PostRequestDto; // DTO import 추가
import com.ch4.lumia_backend.entity.Post;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 어노테이션 추가

@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;

    /**
     * 게시글 목록 조회 (페이징)
     */
    @Transactional(readOnly = true) // 조회 작업이므로 readOnly = true 설정
    public Page<Post> getPosts(int page, int size) {
        logger.debug("Fetching posts - page: {}, size: {}", page, size);
        // ID를 기준으로 내림차순 정렬 (최신글부터)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return postRepository.findAll(pageable);
    }

    /**
     * 게시글 작성
     */
    @Transactional // 데이터 변경 작업이므로 @Transactional 추가
    public Post createPost(String category, String title, String content, User user) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 비어 있을 수 없습니다.");
        }
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 유효하지 않습니다. 로그인이 필요합니다.");
        }
        // 카테고리가 비어있거나 null일 경우 기본값 또는 예외 처리 (선택적)
        if (category == null || category.trim().isEmpty()) {
            // 예: category = "기타"; // 기본값 설정
            throw new IllegalArgumentException("카테고리는 비어 있을 수 없습니다.");
        }


        Post post = Post.builder()
                .category(category)
                .title(title)
                .content(content)
                .author(user)
                .build();

        logger.info("User {} is creating a new post with title: {}", user.getUserId(), title);
        return postRepository.save(post);
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Post not found with id: {}", id);
                    return new IllegalArgumentException("ID " + id + "에 해당하는 게시글이 존재하지 않습니다."); // ID 명시
                });
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public Post updatePost(Long id, PostRequestDto postDto, User user) { // DTO 타입 명시
        Post post = getPostById(id); // 내부적으로 findById 사용, 못 찾으면 예외 발생

        if (!post.getAuthor().getId().equals(user.getId())) {
            logger.warn("User {} attempted to update post {} owned by user {}, but has no permission.",
                        user.getUserId(), id, post.getAuthor().getUserId());
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        // DTO에서 받은 값으로 업데이트
        // 카테고리, 제목, 내용 중 하나라도 비어있으면 예외처리 (선택적)
        if (postDto.getCategory() == null || postDto.getCategory().trim().isEmpty()){
            throw new IllegalArgumentException("카테고리는 비어 있을 수 없습니다.");
        }
        if (postDto.getTitle() == null || postDto.getTitle().trim().isEmpty()){
            throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
        }
        if (postDto.getContent() == null || postDto.getContent().trim().isEmpty()){
            throw new IllegalArgumentException("내용은 비어 있을 수 없습니다.");
        }

        post.update(postDto.getCategory(), postDto.getTitle(), postDto.getContent());
        logger.info("Post {} updated by user {}", id, user.getUserId());
        return postRepository.save(post);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long id, User user) {
        Post post = getPostById(id); // 내부적으로 findById 사용, 못 찾으면 예외 발생

        if (!post.getAuthor().getId().equals(user.getId())) {
            logger.warn("User {} attempted to delete post {} owned by user {}, but has no permission.",
                        user.getUserId(), id, post.getAuthor().getUserId());
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }
        logger.info("Post {} deleted by user {}", id, user.getUserId());
        postRepository.delete(post);
    }
}