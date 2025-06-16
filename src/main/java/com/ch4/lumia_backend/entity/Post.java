// src/main/java/com/ch4/lumia_backend/entity/Post.java
package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity // 이 클래스가 JPA 엔티티임을 나타냅니다.
@Table(name = "posts") // 데이터베이스의 "posts" 테이블과 매핑됩니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자를 필요로 합니다. protected 접근 수준으로 설정합니다.
public class Post extends BaseTimeEntity { // 생성 및 수정 시간을 자동으로 관리하는 BaseTimeEntity 상속

    @Id // 기본 키(Primary Key) 필드임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 데이터베이스가 ID를 자동으로 생성하도록 합니다 (예: auto_increment).
    private Long id;

    @Column(nullable = false) // null 값을 허용하지 않습니다.
    private String category; // 게시글 카테고리

    @Column(nullable = false, length = 100) // null 비허용, 최대 길이 100
    private String title; // 게시글 제목

    @Column(nullable = false, length = 255) // null 비허용, 최대 길이 255 (더 긴 내용이 필요하면 @Lob 사용 고려)
    private String content; // 게시글 내용

    // User 엔티티와의 다대일(N:1) 관계 설정
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩(LAZY)으로 설정하여, 실제 author 정보가 필요할 때만 데이터베이스에서 조회합니다.
    @JoinColumn(name = "user_pk_id", nullable = false) // 외래 키 컬럼의 이름을 "user_pk_id"로 지정하고, null 비허용.
    private User author; // 게시글 작성자

    // Comment 엔티티와의 일대다(1:N) 관계 설정
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    // "mappedBy = "post""는 Comment 엔티티에 있는 "post" 필드에 의해 이 관계가 관리됨을 의미합니다.
    // "cascade = CascadeType.ALL"은 Post 엔티티의 변경(저장, 삭제 등)이 연관된 Comment 엔티티에도 전파되도록 합니다.
    // "orphanRemoval = true"는 Post 엔티티에서 Comment가 제거되면(컬렉션에서 삭제되면) 데이터베이스에서도 해당 Comment가 삭제되도록 합니다.
    private List<Comment> comments = new ArrayList<>(); // 해당 게시글의 댓글 목록

    @Builder // 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
    public Post(String category, String title, String content, User author) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.author = author;
    }

    /**
     * ID만 가진 Post 인스턴스를 생성 (주로 다른 엔티티에서 관계 설정 시 프록시처럼 사용)
     */
    public static Post fromId(Long id) {
        Post post = new Post();
        // JPA에서 ID만 설정된 객체는 기존 엔티티를 참조하는 데 사용될 수 있습니다.
        // 다만, 이 객체로 다른 필드에 접근하려 하면 문제가 발생할 수 있으니 주의해야 합니다.
        post.id = id;
        return post;
    }

    /**
     * 게시글 정보 업데이트 메소드
     */
    public void update(String category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }
}