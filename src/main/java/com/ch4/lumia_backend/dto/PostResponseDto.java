// src/main/java/com/ch4/lumia_backend/dto/PostResponseDto.java
package com.ch4.lumia_backend.dto;

import com.ch4.lumia_backend.entity.Post;
import lombok.Getter;

import java.time.format.DateTimeFormatter; // DateTimeFormatter 추가 (선택적)

@Getter
public class PostResponseDto {
    private Long id;
    private String category;
    private String title;
    private String content;
    private String createdAt;
    private String userId; // 작성자 ID

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.content = post.getContent();
        // createdAt을 특정 형식의 문자열로 보내고 싶다면 DateTimeFormatter 사용 가능
        // 예: this.createdAt = post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.createdAt = post.getCreatedAt().toString(); // 기본 ISO 8601 형식 문자열
        this.userId = post.getAuthor().getUserId(); // 작성자 ID 매핑
    }
}