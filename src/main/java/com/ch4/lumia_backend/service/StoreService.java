package com.ch4.lumia_backend.service;

import com.ch4.lumia_backend.dto.PurchaseRequestDto;
import com.ch4.lumia_backend.entity.User;
import com.ch4.lumia_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final UserRepository userRepository;

    // 참고: 이상적으로는 아이템 목록과 가격도 DB에서 관리해야 하지만,
    // 지금은 프론트엔드와 동일한 목록을 임시로 서비스에 넣어서 구현합니다.
    private static final Map<String, Integer> ITEM_PRICES = Map.of(
            "shirtpink", 20,
            "shirtblue", 20,
            "shirtorange", 20,
            "hat1", 10,
            "hat2", 10,
            "hat3", 10
    );

    @Transactional
    public void purchaseItem(String userId, PurchaseRequestDto purchaseRequest) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String itemId = purchaseRequest.getItemId();
        String itemName = purchaseRequest.getItemName();
        int cost = purchaseRequest.getCost();

        // 서버에 저장된 가격과 요청 가격이 일치하는지 확인 (보안 강화)
        if (!Objects.equals(ITEM_PRICES.get(itemId), cost)) {
            throw new IllegalArgumentException("아이템 가격 정보가 일치하지 않습니다.");
        }

        // 이미 구매한 아이템인지 확인
        if (user.getPurchasedItems().contains(itemName)) {
            throw new IllegalStateException("이미 구매한 아이템입니다.");
        }

        // 코인이 충분한지 확인
        if (user.getCoin() < cost) {
            throw new IllegalStateException("코인이 부족합니다.");
        }

        // 코인 차감 및 아이템 추가
        user.setCoin(user.getCoin() - cost);
        user.getPurchasedItems().add(itemName);
        // @Transactional에 의해 user 객체는 자동 저장됩니다.
    }
}