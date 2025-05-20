// src/main/java/com/ch4/lumia_backend/config/SecurityConfig.java
package com.ch4.lumia_backend.config;

import com.ch4.lumia_backend.security.jwt.JwtAuthenticationFilter; // 가정: JWT 인증 필터
import com.ch4.lumia_backend.security.jwt.JwtUtil; // JWT 유틸리티
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // JwtUtil 주입을 위해
public class SecurityConfig {

    private final JwtUtil jwtUtil; // JwtUtil 주입

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (Stateless API 서버)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()       // 로그인, 회원가입은 모두 허용
                .requestMatchers("/api/users/me/settings").authenticated() // 사용자 설정은 인증 필요
                .requestMatchers("/api/questions/for-me").authenticated() // 사용자별 질문은 인증 필요
                .requestMatchers("/api/answers/**").authenticated()    // 답변 관련 API는 인증 필요
                // .requestMatchers("/h2-console/**").permitAll()    // H2 콘솔 사용 시 (개발용)
                .anyRequest().permitAll() // 개발 초기에는 나머지 요청을 임시로 허용, 추후 보안 강화 필요
                // .anyRequest().authenticated() // 프로덕션에서는 인증되지 않은 다른 모든 요청 거부
            );
            // H2 콘솔 사용 시 필요한 설정 (프로덕션에서는 제거)
            // .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 추가
        // JwtAuthenticationFilter는 직접 구현해야 합니다. (요청에서 JWT 토큰을 읽어 SecurityContext에 인증 정보 설정)
        // 이 필터가 없으면 authenticated()로 보호된 API 접근 시 403 Forbidden 발생 가능성이 높습니다.
        // 여기서는 JwtUtil을 사용하는 간단한 필터를 가정합니다. 실제 구현은 더 정교해야 합니다.
        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}