package com.jeja.jejabe.config;

import com.jeja.jejabe.auth.UserDetailsServiceImpl;
import com.jeja.jejabe.global.jwt.JwtAuthenticationFilter;
import com.jeja.jejabe.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] SWAGGER_URL_PATTERNS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs.yaml"
    };

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
            ROLE_ADMIN > ROLE_PASTOR
            ROLE_ADMIN > ROLE_TEAM_LEADER
            ROLE_ADMIN > ROLE_CELL_LEADER
            ROLE_ADMIN > ROLE_EXECUTIVE
            
            ROLE_PASTOR > ROLE_USER
            ROLE_TEAM_LEADER > ROLE_USER
            ROLE_SOONJANG > ROLE_USER
            ROLE_EXECUTIVE > ROLE_USER
        """);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // 1. Swagger (API 문서)
                        .requestMatchers(SWAGGER_URL_PATTERNS).permitAll()

                        // 2. 인증/가입 관련
                        .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()

                        // 3. 출석 체크 (비로그인/공용 태블릿 사용 가능성 고려)
                        .requestMatchers("/api/schedule/*/check-in").permitAll()

                        // 4. ★ 정적 리소스 및 파일 업로드 경로 (이미지 보기용)
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()

                        // 5. ★ 홈페이지 공개 데이터 (슬라이드, 유튜브 설정 등)
                        .requestMatchers("/api/homepage/**").permitAll()

                        // 6. ★ 게시판/게시글 조회 (GET 요청만 허용 -> 공개 게시판은 비로그인도 볼 수 있게)
                        .requestMatchers(HttpMethod.GET, "/api/boards/**", "/api/posts/**", "/api/comments/**").permitAll()

                        // 7. 관리자 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 8. 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173","https://jeja.shop","http://60.196.100.101"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 요청 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 응답 헤더 노출
        configuration.setExposedHeaders(List.of("Authorization"));

        // 쿠키/인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
