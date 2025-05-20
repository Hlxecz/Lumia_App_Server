package com.ch4.lumia_backend.entity; // 패키지 경로는 실제 구조에 맞게 수정하세요

import jakarta.persistence.*;
// import lombok.AccessLevel; // NoArgsConstructor 관련이라 삭제
import lombok.Builder; // Builder는 사용하므로 유지
import lombok.Getter; // Getter는 사용하므로 유지
// import lombok.NoArgsConstructor; // 직접 생성자를 만들었으므로 삭제

@Getter // 필드 Getter 자동 생성 (문제 없었음)
// @NoArgsConstructor(access = AccessLevel.PROTECTED) // <<< 문제의 원인이었으므로 삭제
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity { // BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_pk_id")
    private Long id; // PK

    @Column(name = "user_login_id", unique = true, nullable = false, length = 50)
    private String userId; // 로그인 아이디

    @Column(nullable = false)
    private String password; // 비밀번호 (암호화 필요)

    @Column(name = "user_name", nullable = false, length = 100)
    private String username; // 이름

    @Column(unique = true, nullable = false) // 이메일 추가
    private String email; // 이메일

    @Column(nullable = false)
    private String role; // 역할

    // === JPA가 요구하는 기본 생성자 직접 추가 ===
    protected User() {
        // JPA가 내부적으로 사용하기 위한 생성자이므로 내용은 비워둡니다.
        // 접근 제한자는 protected 또는 public이어야 합니다. (JPA 스펙상 권장)
    }
    // =======================================

    // Builder 패턴을 사용하기 위한 생성자 (Lombok @Builder 어노테이션이 이 생성자를 사용)
    @Builder
    public User(String userId, String password, String username, String email, String role) {
        this.userId = userId;
        this.password = password; // 실제로는 암호화된 값 저장 필요
        this.username = username;
        this.email = email;
        this.role = role;
        // createdAt, updatedAt 필드는 BaseTimeEntity와 Auditing 기능이 자동으로 처리
    }
}