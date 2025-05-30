// src/main/java/com/ch4/lumia_backend/entity/User.java
package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter; // 필드 수정을 위해 Setter 추가

@Getter
@Setter // <<< 필드 수정을 위해 Setter 추가
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_pk_id")
    private Long id;

    @Column(name = "user_login_id", unique = true, nullable = false, length = 50)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(name = "user_name", nullable = false, length = 100)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    // === 새로운 프로필 필드 추가 ===
    @Column(length = 20) // 예: "MALE", "FEMALE", "OTHER", "NOT_SPECIFIED" 등 유연하게
    private String gender;

    @Column(length = 10) // 예: "A_POSITIVE", "B_NEGATIVE", "O_POSITIVE", "AB_POSITIVE" 등
    private String bloodType;

    @Column(length = 10) // 예: "ISTJ", "ENFP"
    private String mbti;
    // ===========================

    protected User() {
    }

    @Builder
    public User(Long id, String userId, String password, String username, String email, String role,
                String gender, String bloodType, String mbti) { // 빌더에도 새 필드 추가 (id도 포함시켜 완전한 빌더로)
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.username = username;
        this.email = email;
        this.role = role;
        this.gender = gender;
        this.bloodType = bloodType;
        this.mbti = mbti;
    }
}