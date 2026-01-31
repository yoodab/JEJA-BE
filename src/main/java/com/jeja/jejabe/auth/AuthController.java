package com.jeja.jejabe.auth;

import com.jeja.jejabe.auth.dto.*;
import com.jeja.jejabe.global.response.ApiResponseForm;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/send-verification-code")
    public ResponseEntity<ApiResponseForm<Void>> sendVerificationCode(@RequestBody VerificationCodeRequestDto requestDto) {
        authService.sendVerificationCode(requestDto.getLoginId(), requestDto.getEmail());
        return ResponseEntity.ok(ApiResponseForm.success("인증번호가 이메일로 전송되었습니다."));
    }

    // 2. 인증번호 확인 (UI에서 '인증하기' 버튼 클릭 시)
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponseForm<Boolean>> verifyCode(@RequestBody VerificationCodeCheckDto requestDto) {
        boolean isVerified = authService.verifyCode(requestDto.getEmail(), requestDto.getAuthCode());
        if (isVerified) {
            return ResponseEntity.ok(ApiResponseForm.success(true, "인증에 성공했습니다."));
        } else {
            return ResponseEntity.badRequest().body(ApiResponseForm.success(false, "인증번호가 일치하지 않거나 만료되었습니다."));
        }
    }

    // 3. 비밀번호 재설정 (최종 '변경하기' 버튼)
    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponseForm<Void>> resetPassword(@RequestBody PasswordResetRequestDto requestDto) {
        authService.resetPasswordWithCode(requestDto.getEmail(), requestDto.getAuthCode(), requestDto.getNewPassword());
        return ResponseEntity.ok(ApiResponseForm.success("비밀번호가 성공적으로 변경되었습니다."));
    }

    @PostMapping("/signup/send-verification")
    public ResponseEntity<ApiResponseForm<Void>> sendSignupVerification(@RequestBody EmailCheckDto requestDto) {
        authService.sendSignupVerificationCode(requestDto.getEmail());
        return ResponseEntity.ok(ApiResponseForm.success("인증번호가 전송되었습니다."));
    }

    // 2. 회원가입용 인증번호 확인
    @PostMapping("/signup/verify")
    public ResponseEntity<ApiResponseForm<Void>> verifySignup(@RequestBody EmailVerificationDto requestDto) {
        authService.verifySignupCode(requestDto.getEmail(), requestDto.getAuthCode());
        return ResponseEntity.ok(ApiResponseForm.success("이메일 인증이 완료되었습니다."));
    }

}
