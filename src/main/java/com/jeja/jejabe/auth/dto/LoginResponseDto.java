package com.jeja.jejabe.auth.dto;

import com.jeja.jejabe.auth.User;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final String name; // 청년 이름
    private final String role; // 사용자 권한 (e.g., "ROLE_ADMIN", "ROLE_LEADER")
    private final String accessToken;
    private final String refreshToken;

    // User 엔티티를 받아서 DTO를 생성하는 생성자
    public LoginResponseDto(User user, String accessToken, String refreshToken) {
        this.name = user.getMember().getName(); // 연결된 Member 엔티티에서 이름을 가져옴
        this.role = user.getUserRole().name();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
