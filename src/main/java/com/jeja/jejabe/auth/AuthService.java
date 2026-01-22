package com.jeja.jejabe.auth;

import com.jeja.jejabe.auth.dto.LoginRequestDto;
import com.jeja.jejabe.auth.dto.LoginResponseDto;
import com.jeja.jejabe.auth.dto.SignupRequestDto;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.global.jwt.JwtUtil;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.MemberStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
        if (userRepository.findByLoginId(requestDto.getLoginId()).isPresent()) {
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
                .member(member)
                .build();

        userRepository.save(newUser);
    }
}