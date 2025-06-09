// src/main/java/com/ch4/lumia_backend/repository/UserAnswerRepository.java
package com.ch4.lumia_backend.repository;

import com.ch4.lumia_backend.entity.Question; // Question import 추가
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    Page<UserAnswer> findByUserOrderByAnsweredAtDesc(User user, Pageable pageable);

    // ======================= ▼▼▼ 메소드 추가 ▼▼▼ =======================
    /**
     * 특정 사용자가 특정 질문에 대해 답변을 했는지 여부를 확인합니다.
     */
    boolean existsByUserAndQuestion(User user, Question question);
    // ======================= ▲▲▲ 메소드 추가 ▲▲▲ =======================
}