package com.ch4.lumia_backend.entity; // 본인의 entity 패키지 경로 확인

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Auditing 리스너
import java.time.LocalDateTime; // Java 8 이상 시간 타입

@Getter
@MappedSuperclass // 이 클래스는 테이블로 직접 매핑되지 않고, 상속하는 자식 클래스에게 필드만 물려줍니다.
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능을 이 클래스에 적용합니다.
public abstract class BaseTimeEntity { // abstract 키워드 추가 (직접 객체 생성 방지)

    @CreatedDate // Entity가 생성되어 저장될 때 시간이 자동으로 저장됩니다.
    @Column(updatable = false) // 생성 시간은 업데이트되지 않도록 설정합니다.
    private LocalDateTime createdAt; // 생성 시기 필드 (DB에는 created_at 컬럼)

    @LastModifiedDate // 조회한 Entity의 값을 변경할 때 시간이 자동으로 저장됩니다.
    private LocalDateTime updatedAt; // 수정 시기 필드 (DB에는 updated_at 컬럼)
}