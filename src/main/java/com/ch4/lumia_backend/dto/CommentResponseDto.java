// src/main/java/com/ch4/lumia_backend/dto/CommentResponseDto.java
package com.ch4.lumia_backend.dto;

import com.ch4.lumia_backend.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // DateTimeFormatter 추가 (선택적)

@Getter
public class CommentResponseDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt; // 프론트엔드에서 new Date(createdAt).toLocaleString() 등으로 사용 예정
    private String userId;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt(); // LocalDateTime 타입 그대로 유지
        this.userId = comment.getUserId();  // Comment 엔티티에서 직접 가져옴
    }
}