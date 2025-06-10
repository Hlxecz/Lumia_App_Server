// src/main/java/com/ch4/lumia_backend/config/SecurityConfig.java
package com.ch4.lumia_backend.config;

import com.ch4.lumia_backend.security.jwt.JwtAuthenticationFilter;
import com.ch4.lumia_backend.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // HttpMethod import 추가
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .anonymous(AnonymousConfigurer::disable) // 익명 사용자 설정 비활성화 (필요에 따라 조정)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .authorizeHttpRequests(authz -> authz
                // 인증 관련 엔드포인트는 모두 허용
                .requestMatchers("/api/users/auth/login", "/api/users/auth/signup", "/api/users/auth/refresh-token").permitAll()

                // 사용자 정보, 질문, 답변 관련 주요 엔드포인트는 인증 필요
                .requestMatchers("/api/users/me/**").authenticated()
                .requestMatchers("/api/questions/**").authenticated()
                .requestMatchers("/api/answers/**").authenticated()

                // 게시글(Posts) 관련 엔드포인트 권한 설정
                .requestMatchers(HttpMethod.GET, "/api/posts/list", "/api/posts/{id}").permitAll() // 목록 및 상세 조회는 모두 허용
                .requestMatchers(HttpMethod.POST, "/api/posts/write").authenticated() // 게시글 작성
                .requestMatchers(HttpMethod.PUT, "/api/posts/{id}").authenticated()    // 게시글 수정
                .requestMatchers(HttpMethod.DELETE, "/api/posts/{id}").authenticated() // 게시글 삭제
                // .requestMatchers("/api/posts/**").authenticated() // 위에서 더 세분화했으므로 이 줄은 필요 없거나, GET을 제외한 나머지를 막는 용도로 사용 가능

                // 댓글(Comments) 관련 엔드포인트 권한 설정
                .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/comments").permitAll() // 특정 게시글의 댓글 조회는 모두 허용
                .requestMatchers(HttpMethod.POST, "/api/posts/{postId}/comments").authenticated() // 댓글 작성
                .requestMatchers(HttpMethod.PUT, "/api/comments/{commentId}").authenticated()    // 댓글 수정
                .requestMatchers(HttpMethod.DELETE, "/api/comments/{commentId}").authenticated() // 댓글 삭제

                // 위에서 명시적으로 처리되지 않은 다른 모든 요청은 일단 허용 (개발 편의를 위함)
                // 프로덕션 환경에서는 .denyAll() 또는 특정 권한으로 변경하는 것을 강력히 권장합니다.
                .anyRequest().permitAll()
            );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}