// src/main/java/com/ch4/lumia_backend/entity/RefreshToken.java
package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant; // UTC 기준 시간을 위해 Instant 사용

@Entity
@Table(name = "refresh_tokens") // 데이터베이스에 생성될 테이블 이름
@Getter
@Setter
@NoArgsConstructor // JPA는 기본 생성자를 필요로 합니다.
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id") // 컬럼명 명시 (선택적)
    private Long id;

    // User 엔티티와 1:1 또는 1:N 관계를 설정합니다.
    // 일반적으로 한 사용자는 하나의 활성 리프레시 토큰을 가지므로 1:1이 적합할 수 있습니다.
    // 또는 여러 기기에서 로그인 허용 후 각 리프레시 토큰을 관리하려면 User에 대한 ManyToOne도 가능합니다.
    // 여기서는 User당 하나의 RefreshToken을 가정하고 OneToOne으로 하겠습니다.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk_id", referencedColumnName = "user_pk_id", nullable = false, unique = true)
    private User user;

    @Column(name = "token_value", nullable = false, unique = true, length = 1024) // 실제 토큰 문자열 (길이 충분히)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate; // 토큰 만료 시간 (UTC 기준)

    // 생성자
    public RefreshToken(User user, String token, Instant expiryDate) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    // (선택적) 토큰 생성 시간을 기록하고 싶다면 BaseTimeEntity를 상속하거나 createdAt 필드를 추가할 수 있습니다.
    // 예: private LocalDateTime createdAt;
    //     @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}