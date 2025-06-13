// src/main/java/com/ch4/lumia_backend/entity/User.java
package com.ch4.lumia_backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_pk_id")
    private Long id;

    // ... (기존 필드들은 동일)
    @Column(name = "user_login_id", unique = true, nullable = false, length = 50)
    private String userId;
    @Column(nullable = false)
    private String password;
    @Column(name = "user_name", nullable = false, length = 100)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String role;
    @Column(length = 20)
    private String gender;
    @Column(length = 10)
    private String bloodType;
    @Column(length = 10)
    private String mbti;
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int coin;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_equipped_items", joinColumns = @JoinColumn(name = "user_pk_id"))
    @Column(name = "item_name", nullable = false)
    private List<String> equippedItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_purchased_items", joinColumns = @JoinColumn(name = "user_pk_id"))
    @Column(name = "item_name", nullable = false)
    private List<String> purchasedItems = new ArrayList<>();

    // ======================= ▼▼▼ 필드 추가 ▼▼▼ =======================
    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private int characterLevel; // 캐릭터 레벨 필드 추가 (기본값 1)
    // ======================= ▲▲▲ 필드 추가 ▲▲▲ =======================


    protected User() {
    }

    @Builder
    public User(Long id, String userId, String password, String username, String email, String role,
                String gender, String bloodType, String mbti, int coin, List<String> equippedItems, List<String> purchasedItems, int characterLevel) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.username = username;
        this.email = email;
        this.role = role;
        this.gender = gender;
        this.bloodType = bloodType;
        this.mbti = mbti;
        this.coin = coin;
        this.equippedItems = equippedItems != null ? equippedItems : new ArrayList<>();
        this.purchasedItems = purchasedItems != null ? purchasedItems : new ArrayList<>();
        // ======================= ▼▼▼ 로직 추가 ▼▼▼ =======================
        this.characterLevel = characterLevel;
        // ======================= ▲▲▲ 로직 추가 ▲▲▲ =======================
    }
}