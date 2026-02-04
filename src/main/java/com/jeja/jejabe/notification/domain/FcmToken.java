package com.jeja.jejabe.notification.domain;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fcm_token")
public class FcmToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Builder
    public FcmToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}
