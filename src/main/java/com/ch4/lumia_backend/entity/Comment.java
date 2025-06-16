// src/main/java/com/ch4/lumia_backend/entity/Comment.java
package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Entity // 이 클래스가 JPA 엔티티임을 나타냅니다.
@Table(name = "comments") // 데이터베이스의 "comments" 테이블과 매핑됩니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자를 필요로 합니다.
public class Comment {

    @Id // 기본 키 필드입니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 자동 생성을 데이터베이스에 위임합니다.
    private Long id;

    // Post 엔티티와의 다대일(N:1) 관계입니다. 하나의 게시글에는 여러 댓글이 달릴 수 있습니다.
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩(LAZY)으로 설정합니다.
    @JoinColumn(name = "post_id", nullable = false) // 외래 키 컬럼의 이름을 "post_id"로 지정하고, null을 허용하지 않습니다.
    private Post post; // 이 댓글이 속한 게시글

    // 댓글 작성자의 ID를 문자열로 직접 저장합니다. User 엔티티와 직접적인 @ManyToOne 관계를 맺지 않았습니다.
    // 이는 Comment 엔티티가 User 엔티티의 다른 정보에 직접 의존하지 않도록 하는 설계일 수 있습니다.
    @Column(nullable = false)
    private String userId; // 댓글 작성자 ID

    @Column(nullable = false, length = 255) // null 비허용, 최대 길이 255
    private String content; // 댓글 내용

    private LocalDateTime createdAt; // 댓글 생성 시간

    @Builder // 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
    public Comment(Post post, String userId, String content) {
        this.post = post;
        this.userId = userId;
        this.content = content;
        this.createdAt = LocalDateTime.now(); // 생성 시 현재 시간으로 초기화
    }

    /**
     * 댓글 내용 수정 메소드
     */
    public void updateContent(String content) {
        this.content = content;
    }
}