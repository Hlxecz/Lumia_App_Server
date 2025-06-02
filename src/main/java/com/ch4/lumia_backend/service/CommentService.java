package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.entity.Comment;
import com.ch4.lumia_backend.entity.Post;
import com.ch4.lumia_backend.repository.CommentRepository;
import com.ch4.lumia_backend.repository.PostRepository; // Post 존재 여부 확인을 위해 추가
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Logger 추가
import org.slf4j.LoggerFactory; // LoggerFactory 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 어노테이션 추가

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class); // Logger 선언

    private final CommentRepository commentRepository;
    private final PostRepository postRepository; // PostRepository 주입

    /**
     * 특정 게시글에 달린 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        // 게시글 존재 여부 먼저 확인 (선택적이지만, 더 안전한 방법)
        if (!postRepository.existsById(postId)) {
            logger.warn("Attempted to get comments for non-existent post with id: {}", postId);
            throw new IllegalArgumentException("ID " + postId + "에 해당하는 게시글이 존재하지 않습니다.");
        }
        Post post = Post.fromId(postId); // ID만 가진 Post 객체 생성
        logger.debug("Fetching comments for post id: {}", postId);
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public Comment createComment(Post postProxy, String userId, String content) { // Post 객체 대신 Post 프록시 또는 ID를 받을 수 있음
        if (content == null || content.trim().isEmpty()) {
            logger.warn("User {} attempted to create a comment with empty content for post id: {}", userId, postProxy.getId());
            throw new IllegalArgumentException("댓글 내용은 비어 있을 수 없습니다.");
        }
        if (userId == null || userId.trim().isEmpty()){
            // 이 경우는 보통 Controller 단에서 인증 정보로 userId를 가져오므로 발생하기 어려움
            logger.error("Attempted to create a comment without a valid userId for post id: {}", postProxy.getId());
            throw new IllegalArgumentException("사용자 정보가 유효하지 않습니다.");
        }

        // 실제 Post 엔티티 조회 (게시글이 실제로 존재하는지 확인)
        Post actualPost = postRepository.findById(postProxy.getId())
                .orElseThrow(() -> {
                    logger.warn("User {} attempted to create a comment for non-existent post with id: {}", userId, postProxy.getId());
                    return new IllegalArgumentException("ID " + postProxy.getId() + "에 해당하는 게시글이 존재하지 않습니다.");
                });

        Comment comment = Comment.builder()
                .post(actualPost) // 실제 조회된 Post 엔티티 사용
                .userId(userId)
                .content(content)
                .build();

        logger.info("User {} created a new comment on post id: {}", userId, actualPost.getId());
        return commentRepository.save(comment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public Comment updateComment(Long commentId, String userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            logger.warn("User {} attempted to update comment id: {} with empty content.", userId, commentId);
            throw new IllegalArgumentException("댓글 내용은 비어 있을 수 없습니다.");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    logger.warn("User {} attempted to update non-existent comment id: {}.", userId, commentId);
                    return new IllegalArgumentException("ID " + commentId + "에 해당하는 댓글이 존재하지 않습니다.");
                });

        if (!comment.getUserId().equals(userId)) {
            logger.warn("User {} attempted to update comment id: {} owned by user {}, but has no permission.",
                        userId, commentId, comment.getUserId());
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.updateContent(content);
        logger.info("Comment id: {} updated by user {}.", commentId, userId);
        return commentRepository.save(comment);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    logger.warn("User {} attempted to delete non-existent comment id: {}.", userId, commentId);
                    return new IllegalArgumentException("ID " + commentId + "에 해당하는 댓글이 존재하지 않습니다.");
                });

        if (!comment.getUserId().equals(userId)) {
            logger.warn("User {} attempted to delete comment id: {} owned by user {}, but has no permission.",
                        userId, commentId, comment.getUserId());
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }
        logger.info("Comment id: {} deleted by user {}.", commentId, userId);
        commentRepository.delete(comment);
    }
}