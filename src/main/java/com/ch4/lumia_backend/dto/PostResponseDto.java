package com.ch4.lumia_backend.dto;

import com.ch4.lumia_backend.entity.Post;
import lombok.Getter;

@Getter
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String userId;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory();

        // Lazy 로딩 문제 방지용 안전처리
        this.userId = (post.getAuthor() != null) ? post.getAuthor().getUserId() : "익명";
    }

}

