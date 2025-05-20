// src/main/java/com/ch4/lumia_backend/repository/UserSettingRepository.java
package com.ch4.lumia_backend.repository;

import com.ch4.lumia_backend.entity.UserSetting;
import com.ch4.lumia_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository import
import java.util.Optional;

// JpaRepository<[관리할 Entity 클래스 이름], [Entity의 @Id 필드 타입]>
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> { // JpaRepository 상속 추가!

    Optional<UserSetting> findByUser(User user);
    Optional<UserSetting> findByUser_Id(Long userId); // user_pk_id (User 엔티티의 id 필드명)로 찾기
    // 또는 Optional<UserSetting> findByUser_UserPkId(Long userPkId); // User 엔티티의 PK 필드명이 userPkId인 경우
}