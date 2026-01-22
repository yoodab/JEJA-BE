package com.jeja.jejabe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

//@SpringBootApplication
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableJpaAuditing
@EnableScheduling
public class JejaBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(JejaBeApplication.class, args);
    }

}
