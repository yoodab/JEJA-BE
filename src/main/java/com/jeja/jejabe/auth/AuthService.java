package com.jeja.jejabe.auth;

import com.jeja.jejabe.auth.dto.LoginRequestDto;
import com.jeja.jejabe.auth.dto.LoginResponseDto;
import com.jeja.jejabe.auth.dto.SignupRequestDto;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.global.jwt.JwtUtil;
import com.jeja.jejabe.global.util.RedisUtil;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.MemberStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final RedisUtil redisUtil;

    private static final String SIGNUP_PREFIX = "SIGNUP_CODE:"; // 인증번호 저장용
    private static final String VERIFIED_PREFIX = "VERIFIED:";

    // 인증번호 유효시간 (5분)
    private static final long CODE_EXPIRATION = 60 * 5L;

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendSignupVerificationCode(String email) {
        // 1. 중복 이메일 체크 (가입된 이메일이면 안됨)
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(CommonErrorCode.DUPLICATE_EMAIL); // 에러코드 정의 필요
        }

        // 2. 인증번호 생성
        String authCode = generateAuthCode();

        // 3. Redis 저장 (키: "SIGNUP_CODE:이메일", 유효시간 5분)
        redisUtil.setDataExpire(SIGNUP_PREFIX + email, authCode, 60 * 5L);

        // 4. 이메일 전송
        emailService.sendSignupVerificationCode(email, authCode);
    }

    /**
     * 2. [회원가입용] 인증번호 검증
     */
    public void verifySignupCode(String email, String code) {
        String redisCode = redisUtil.getData(SIGNUP_PREFIX + email);

        if (redisCode == null || !redisCode.equals(code)) {
            throw new GeneralException(CommonErrorCode.INVALID_AUTH_CODE); // "인증번호 불일치/만료"
        }

        // 인증 성공 시, "인증된 이메일임"을 증명하는 데이터를 Redis에 저장 (유효시간 20분)
        // 나중에 최종 가입 요청 때 이 키가 있는지 확인합니다.
        redisUtil.setDataExpire(VERIFIED_PREFIX + email, "TRUE", 60 * 20L);

        // 사용된 인증번호는 삭제
        redisUtil.deleteData(SIGNUP_PREFIX + email);
    }

    @Transactional(readOnly = true) // Member 정보를 조회하므로 트랜잭션 필요
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {
        User user = userRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new GeneralException(CommonErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            // 실제 운영에서는 "아이디 또는 비밀번호가 일치하지 않습니다."로 통일하는 것이 보안상 더 좋습니다.
            throw new GeneralException(CommonErrorCode.INVALID_PASSWORD);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new GeneralException(CommonErrorCode.ACCOUNT_INACTIVE);
        }

        // 1. JWT 토큰 생성
        String token = jwtUtil.createToken(user.getLoginId(), user.getUserRole().name());

        // 2. 응답 헤더에 토큰 추가
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);

        // 3. 응답 바디에 담을 사용자 정보(DTO) 생성 후 반환
        return new LoginResponseDto(user);
    }


    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 1. 아이디 중복 검사
        if (!redisUtil.hasKey(VERIFIED_PREFIX + requestDto.getEmail())) {
            throw new GeneralException(CommonErrorCode.EMAIL_NOT_VERIFIED); // "이메일 인증이 필요합니다."
        }

        // 2. 아이디 중복 체크 등 기존 로직...
        if (userRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new GeneralException(CommonErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 2. Member 처리 로직 (전화번호로 검색)
        // 전화번호로 기존 멤버를 찾고, 없으면 비활성 상태로 새로 생성
        Member member = memberRepository.findByPhone(requestDto.getPhone())
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .name(requestDto.getName())
                            .phone(requestDto.getPhone())
                            .birthDate(requestDto.getBirthDate())
                            .memberStatus(MemberStatus.INACTIVE)
                            .build();
                    return memberRepository.save(newMember);
                });

        // 3. User 생성 및 Member 연결
        User newUser = User.builder()
                .loginId(requestDto.getLoginId())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .userRole(UserRole.ROLE_USER)
                .status(UserStatus.PENDING)
                .email(requestDto.getEmail())
                .member(member)
                .build();

        userRepository.save(newUser);

        redisUtil.deleteData(VERIFIED_PREFIX + requestDto.getEmail());
    }

    /**
     * 1. 인증번호 발송
     */
    @Transactional
    public void sendVerificationCode(String loginId, String email) {
        // 유저 확인
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equals(email)) {
            throw new GeneralException(CommonErrorCode.BAD_REQUEST); // 이메일 불일치
        }

        // 인증번호 생성 (6자리)
        String authCode = generateAuthCode();

        // Redis 저장 (Key: email, Value: authCode)
        redisUtil.setDataExpire(email, authCode, CODE_EXPIRATION);

        // 이메일 발송
        emailService.sendVerificationCode(email, authCode);
    }

    /**
     * 2. 인증번호 검증 (화면 표시용)
     */
    public boolean verifyCode(String email, String code) {
        String storedCode = redisUtil.getData(email);
        if (storedCode == null) {
            return false; // 만료됨
        }
        return storedCode.equals(code);
    }

    /**
     * 3. 비밀번호 재설정
     */
    @Transactional
    public void resetPasswordWithCode(String email, String code, String newPassword) {
        // 1. 인증번호 재검증 (보안상 필수)
        String storedCode = redisUtil.getData(email);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new GeneralException(CommonErrorCode.BAD_REQUEST); // "인증번호가 만료되었거나 일치하지 않습니다."
        }

        // 2. 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.USER_NOT_FOUND));

        // 3. 비밀번호 변경
        user.changePassword(passwordEncoder.encode(newPassword));

        // 4. 사용한 인증번호 삭제 (재사용 방지)
        redisUtil.deleteData(email);
    }

    // 6자리 랜덤 숫자 생성
    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900000) + 100000; // 100000 ~ 999999
        return String.valueOf(num);
    }
}