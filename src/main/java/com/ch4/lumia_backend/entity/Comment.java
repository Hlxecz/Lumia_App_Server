package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Post와 연관 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 작성자 ID (지금은 user 엔티티 없이 userId만 문자열로 저장)
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 255)
    private String content;

    private LocalDateTime createdAt;

    @Builder
    public Comment(Post post, String userId, String content) {
        this.post = post;
        this.userId = userId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
    }

}
