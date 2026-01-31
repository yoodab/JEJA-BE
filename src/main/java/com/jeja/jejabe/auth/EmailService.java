package com.jeja.jejabe.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendSignupVerificationCode(String to, String authCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[제자교회] 회원가입 인증번호 안내");
        message.setText("안녕하세요.\n\n" +
                "요청하신 인증번호는 [" + authCode + "] 입니다.\n" +
                "5분 안에 입력해 주세요.");

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 전송 실패");
        }
    }
    public void sendVerificationCode(String to, String authCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[제자교회] 비밀번호 찾기 인증번호 안내");
        message.setText("안녕하세요.\n\n" +
                "요청하신 인증번호는 [" + authCode + "] 입니다.\n" +
                "5분 안에 입력해 주세요.");

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 전송 실패");
        }
    }
}
