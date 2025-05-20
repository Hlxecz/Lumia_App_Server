// src/main/java/com/ch4/lumia_backend/repository/UserAnswerRepository.java
package com.ch4.lumia_backend.repository;

import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.entity.UserAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // @Repository 어노테이션은 선택 사항 (Spring Boot에서는 자동 인식)

@Repository // Spring Bean으로 등록 (선택 사항이지만 명시적으로 추가 권장)
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    // 특정 사용자의 모든 답변을 답변 시간(answeredAt) 기준으로 내림차순 정렬하여 페이징 처리된 결과로 가져옴
    Page<UserAnswer> findByUserOrderByAnsweredAtDesc(User user, Pageable pageable);

    // (필요시 추가 메소드 정의)
    // 예: 특정 질문에 대한 모든 사용자 답변 조회
    // List<UserAnswer> findByQuestion(Question question);
}