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
                // ▼▼▼ "/api/users/auth/find-id" 추가 ▼▼▼
                .requestMatchers("/api/users/auth/login", "/api/users/auth/signup", "/api/users/auth/refresh-token", "/api/users/auth/find-id").permitAll()
                // ▲▲▲ "/api/users/auth/find-id" 추가 ▲▲▲

                .requestMatchers("/api/users/me/**").authenticated()
                .requestMatchers("/api/questions/**").authenticated()
                .requestMatchers("/api/answers/**").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/posts/list", "/api/posts/{id}").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/posts/write").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/posts/{id}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/{id}").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/comments").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/posts/{postId}/comments").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/comments/{commentId}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/comments/{commentId}").authenticated()
                
                .anyRequest().permitAll() // 개발 중에는 permitAll, 배포 시에는 denyAll 또는 특정 권한으로 변경 권장
            );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}