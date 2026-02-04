package com.jeja.jejabe.notification.repository;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.notification.domain.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByToken(String token);
    List<FcmToken> findByUser(User user);
    void deleteByToken(String token);
    void deleteByUser(User user);
}
