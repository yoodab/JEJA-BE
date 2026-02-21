package com.jeja.jejabe;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

//@SpringBootApplication
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableJpaAuditing
@EnableScheduling
public class JejaBeApplication {

    @PostConstruct
    public void init() {
        // 서버 시간대를 한국 시간으로 고정 (UTC 문제 방지)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("Set TimeZone to Asia/Seoul");
    }

    public static void main(String[] args) {
        SpringApplication.run(JejaBeApplication.class, args);
    }

}
