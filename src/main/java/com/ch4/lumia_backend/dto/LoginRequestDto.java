package com.ch4.lumia_backend.dto; // 패키지 경로 확인

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // Controller에서 @RequestBody로 JSON 데이터를 이 객체로 변환하려면 Setter 또는 모든 필드를 포함한 생성자가 필요합니다.
public class LoginRequestDto {
    private String userId; // React Native에서 보낼 아이디 필드 이름과 일치시키는 것이 좋음
    private String password; // React Native에서 보낼 비밀번호 필드 이름과 일치시키는 것이 좋음
}