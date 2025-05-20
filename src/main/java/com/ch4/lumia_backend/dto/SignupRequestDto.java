package com.ch4.lumia_backend.dto; // dto 패키지 경로 확인

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String userId;    // 아이디
    private String password;  // 비밀번호
    private String username;  // 이름
    private String email;     // 이메일
    // 필요한 다른 정보가 있다면 필드 추가 가능
}