// src/main/java/com/ch4/lumia_backend/repository/UserAnswerRepository.java
package com.ch4.lumia_backend.repository;

import com.ch4.lumia_backend.entity.Question;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    Page<UserAnswer> findByUserOrderByAnsweredAtDesc(User user, Pageable pageable);

    boolean existsByUserAndQuestion(User user, Question question);

    /**
     * 특정 사용자의 전체 답변 개수를 계산합니다.
     */
    long countByUser(User user);
}