// src/main/java/com/ch4/lumia_backend/repository/UserRepository.java
package com.ch4.lumia_backend.repository;

import com.ch4.lumia_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    // 이메일로 사용자를 찾는 메소드 추가
    Optional<User> findByEmail(String email);
}