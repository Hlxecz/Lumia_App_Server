// src/main/java/com/ch4/lumia_backend/repository/QuestionRepository.java
package com.ch4.lumia_backend.repository;

import com.ch4.lumia_backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 특정 타입의 활성화된 질문 중 랜덤하게 하나 가져오기 (MySQL/MariaDB용 RAND())
    @Query(value = "SELECT * FROM questions WHERE question_type = :questionType AND is_active = true ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomActiveQuestionByType(@Param("questionType") String questionType);

    // 활성화된 특정 타입의 모든 질문 가져오기 (선택적)
    List<Question> findByQuestionTypeAndIsActiveTrue(String questionType);

    // 기본 대체 질문 (가장 최근에 추가된 활성 질문)
    Optional<Question> findFirstByIsActiveTrueOrderByIdDesc();
}