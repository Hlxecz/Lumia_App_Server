package com.ch4.lumia_backend.repository; // 1번에서 만든 패키지 경로 확인

import com.ch4.lumia_backend.entity.User; // User 엔티티의 정확한 경로 import
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<[관리할 Entity 클래스 이름], [Entity의 @Id 필드 타입]>
public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 ID로 사용자를 찾는 메소드 정의
    // 메소드 이름을 "findBy + [Entity의 필드 이름]" 형식으로 만들면
    // Spring Data JPA가 메소드 이름을 분석하여 자동으로 해당 필드로 검색하는 쿼리를 생성하고 실행해줍니다.
    // User Entity에 'userId' 필드가 있으므로 'findByUserId' 라고 짓습니다.
    Optional<User> findByUserId(String userId);

    // 필요하다면 다른 조건으로 검색하는 메소드도 추가할 수 있습니다.
    // 예: 이메일로 사용자 찾기
    // Optional<User> findByEmail(String email);
}