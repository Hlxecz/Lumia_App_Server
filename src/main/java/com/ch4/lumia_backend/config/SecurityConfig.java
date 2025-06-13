// src/main/java/com/ch4/lumia_backend/config/SecurityConfig.java
package com.ch4.lumia_backend.config;

import com.ch4.lumia_backend.security.jwt.JwtAuthenticationFilter;
import com.ch4.lumia_backend.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
            .anonymous(AnonymousConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .authorizeHttpRequests(authz -> authz
                // 인증 관련 엔드포인트는 모두 허용
                .requestMatchers("/api/users/auth/**").permitAll()

                // 사용자 정보, 질문, 답변 관련 주요 엔드포인트는 인증 필요
                .requestMatchers("/api/users/me/**").authenticated()
                .requestMatchers("/api/questions/**").authenticated()
                .requestMatchers("/api/answers/**").authenticated()

                // ======================= ▼▼▼ 규칙 추가 ▼▼▼ =======================
                .requestMatchers("/api/store/**").authenticated() // 상점 관련 API는 인증 필요
                // ======================= ▲▲▲ 규칙 추가 ▲▲▲ =======================

                // 게시글(Posts) 관련 엔드포인트 권한 설정
                .requestMatchers(HttpMethod.GET, "/api/posts/list", "/api/posts/{id}").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/posts/write").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/posts/{id}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/{id}").authenticated()

                // 댓글(Comments) 관련 엔드포인트 권한 설정
                .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/comments").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/posts/{postId}/comments").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/comments/{commentId}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/comments/{commentId}").authenticated()

                // 위에서 명시적으로 처리되지 않은 다른 모든 요청은 일단 허용 (개발 편의를 위함)
                .anyRequest().permitAll()
            );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}