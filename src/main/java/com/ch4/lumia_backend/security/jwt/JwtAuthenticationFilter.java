// src/main/java/com/ch4/lumia_backend/security/jwt/JwtAuthenticationFilter.java
// 이 파일은 직접 생성하고 내용을 채워야 합니다. 아래는 매우 기본적인 골격 예시입니다.
package com.ch4.lumia_backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService; // 필요시 주입
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.springframework.security.core.Authentication; // 추가
import org.springframework.security.core.context.SecurityContextHolder; // 추가
import java.util.Collections; // 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 추가


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    // private final UserDetailsService userDetailsService; // 사용자 정보를 DB에서 직접 조회할 경우 필요

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = resolveToken(request);
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) { // validateToken 구현 필요
                String userId = jwtUtil.getUserIdFromToken(jwt); // getUserIdFromToken 구현 필요

                // 실제로는 UserDetailsService를 통해 UserDetails 객체를 로드하고 권한을 설정해야 함
                // 여기서는 간단히 userId를 Principal로 하는 Authentication 객체 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()); // 사용자 ID, 비밀번호(null), 권한 목록
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 로깅 또는 특정 예외 처리
            SecurityContextHolder.clearContext(); // 예외 발생 시 컨텍스트 클리어
            // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            // return;
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}