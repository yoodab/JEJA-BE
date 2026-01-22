package com.jeja.jejabe.auth;

import com.jeja.jejabe.auth.dto.LoginRequestDto;
import com.jeja.jejabe.auth.dto.LoginResponseDto;
import com.jeja.jejabe.auth.dto.SignupRequestDto;
import com.jeja.jejabe.global.response.ApiResponseForm;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseForm<Void>>  signup(@RequestBody SignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        String message = "회원가입 요청이 완료되었습니다. 관리자 승인을 기다려주세요.";
        return ResponseEntity.ok(ApiResponseForm.success(message));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseForm<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        LoginResponseDto userInfo = authService.login(loginRequestDto, response);
        return ResponseEntity.ok(ApiResponseForm.success(userInfo, "로그인 성공"));
    }
}
