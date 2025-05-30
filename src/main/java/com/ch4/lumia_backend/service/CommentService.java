package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.entity.Comment;
import com.ch4.lumia_backend.entity.Post;
import com.ch4.lumia_backend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    /**
     * 게시글에 달린 댓글 목록 조회
     */
    public List<Comment> getCommentsByPostId(Long postId) {
        Post post = Post.fromId(postId);
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }

    /**
     * 댓글 작성
     */
    public Comment createComment(Post post, String userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 비어 있을 수 없습니다.");
        }

        Comment comment = Comment.builder()
                .post(post)
                .userId(userId)
                .content(content)
                .build();

        return commentRepository.save(comment);
    }

    /**
     * 댓글 수정
     */
    public Comment updateComment(Long commentId, String userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.updateContent(content);
        return commentRepository.save(comment);
    }

    /**
     * 댓글 삭제
     */
    public void deleteComment(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
