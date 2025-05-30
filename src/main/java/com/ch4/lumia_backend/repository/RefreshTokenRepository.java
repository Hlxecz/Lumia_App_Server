// src/main/java/com/ch4/lumia_backend/repository/RefreshTokenRepository.java
package com.ch4.lumia_backend.repository;

import com.ch4.lumia_backend.entity.RefreshToken;
import com.ch4.lumia_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user); // <--- 반환 타입이 void 입니다.
}