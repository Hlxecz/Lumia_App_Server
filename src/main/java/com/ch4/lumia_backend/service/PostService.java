package com.ch4.lumia_backend.service;

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

@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;

    /**
     * 게시글 목록 조회 (페이징)
     */
    public Page<Post> getPosts(int page, int size) {
        logger.debug("Fetching posts - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return postRepository.findAll(pageable);
    }

    /**
     * 게시글 작성
     */
    public Post createPost(String category, String title, String content, User user) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 비어 있을 수 없습니다.");
        }
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 유효하지 않습니다.");
        }

        Post post = Post.builder()
                .category(category)
                .title(title)
                .content(content)
                .author(user)
                .build();

        return postRepository.save(post);
    }

    /**
     * 게시글 상세 조회
     */
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
    }

    /**
     * 게시글 수정
     */
    public Post updatePost(Long id, com.ch4.lumia_backend.dto.PostRequestDto postDto, User user) {
        Post post = getPostById(id);

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.update(postDto.getCategory(), postDto.getTitle(), postDto.getContent());
        return postRepository.save(post);
    }

    /**
     * 게시글 삭제
     */
    public void deletePost(Long id, User user) {
        Post post = getPostById(id);

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}
