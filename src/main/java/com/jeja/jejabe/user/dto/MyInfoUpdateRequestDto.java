package com.jeja.jejabe.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyInfoUpdateRequestDto {
    private String phone;
    private String email;
    private String memberImageUrl; // 기존 멤버 이미지 (유지)
    private String profileImageUrl; // 유저 프로필 이미지 (신규)
    
    // 비밀번호 변경을 원할 경우 (null이면 변경 안함)
    private String currentPassword;
    private String newPassword;
}
