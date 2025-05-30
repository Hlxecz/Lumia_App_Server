// src/main/java/com/ch4/lumia_backend/repository/PostRepository.java

package com.ch4.lumia_backend.repository;

import com.ch4.lumia_backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 필요 시: 특정 작성자의 게시글만 조회
    // Page<Post> findAllByAuthor(User user, Pageable pageable);
}
