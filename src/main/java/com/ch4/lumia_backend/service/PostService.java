// src/main/java/com/ch4/lumia_backend/service/PostService.java
package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.PostRequestDto;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<Post> getPosts(int page, int size) {
        logger.debug("Fetching posts - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return postRepository.findAll(pageable);
    }

    @Transactional
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
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리는 비어 있을 수 없습니다.");
        }

        Post post = Post.builder()
                .category(category)
                .title(title)
                .content(content)
                .author(user)
                .build();
        
        Post savedPost = postRepository.save(post);
        logger.info("User {} is creating a new post with title: {}", user.getUserId(), title);
        
        // 게시글 작성 시 코인 1개 지급
        user.setCoin(user.getCoin() + 1);
        logger.info("Awarded 1 coin to user {}. Total coins: {}", user.getUserId(), user.getCoin());

        return savedPost;
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Post not found with id: {}", id);
                    return new IllegalArgumentException("ID " + id + "에 해당하는 게시글이 존재하지 않습니다.");
                });
    }

    @Transactional
    public Post updatePost(Long id, PostRequestDto postDto, User user) {
        Post post = getPostById(id);

        if (!post.getAuthor().getId().equals(user.getId())) {
            logger.warn("User {} attempted to update post {} owned by user {}, but has no permission.",
                        user.getUserId(), id, post.getAuthor().getUserId());
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

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

    @Transactional
    public void deletePost(Long id, User user) {
        Post post = getPostById(id);

        if (!post.getAuthor().getId().equals(user.getId())) {
            logger.warn("User {} attempted to delete post {} owned by user {}, but has no permission.",
                        user.getUserId(), id, post.getAuthor().getUserId());
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }
        logger.info("Post {} deleted by user {}", id, user.getUserId());
        postRepository.delete(post);
    }
}