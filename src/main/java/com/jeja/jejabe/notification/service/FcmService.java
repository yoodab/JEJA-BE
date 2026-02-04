package com.jeja.jejabe.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserRepository;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.global.exception.ErrorCode;
import com.jeja.jejabe.notification.domain.FcmToken;
import com.jeja.jejabe.notification.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveToken(User user, String token) {
        Optional<FcmToken> existingToken = fcmTokenRepository.findByToken(token);

        if (existingToken.isPresent()) {
            FcmToken fcmToken = existingToken.get();
            if (!fcmToken.getUser().getId().equals(user.getId())) {
                // Token exists but belongs to another user (e.g. logout/login on same device)
                fcmTokenRepository.delete(fcmToken);
                fcmTokenRepository.save(FcmToken.builder().user(user).token(token).build());
            }
        } else {
            fcmTokenRepository.save(FcmToken.builder().user(user).token(token).build());
        }
    }

    @Transactional
    public void deleteToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }

    public void sendNotification(com.jeja.jejabe.notification.dto.NotificationSendRequestDto requestDto) {
        List<User> targets;

        switch (requestDto.getTargetType()) {
            case USER:
                targets = userRepository.findAllByMemberIds(requestDto.getTargetMemberIds());
                break;
            case SOONJANG:
                targets = userRepository.findAllSoonjangs();
                break;
            case ALL:
                // Not implemented yet or strictly required by current prompt, but safe to leave
                // empty or throw
                targets = java.util.Collections.emptyList();
                break;
            default:
                throw new GeneralException(com.jeja.jejabe.global.exception.CommonErrorCode.BAD_REQUEST);
        }

        for (User user : targets) {
            sendToUser(user, requestDto.getTitle(), requestDto.getBody());
        }
    }

    public void sendNotificationToUser(Long userId, String title, String body) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new GeneralException(com.jeja.jejabe.global.exception.CommonErrorCode.USER_NOT_FOUND));
        sendToUser(user, title, body);
    }

    private void sendToUser(User user, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findByUser(user);

        if (tokens.isEmpty()) {
            log.info("No FCM tokens found for user: {}", user.getId());
            return;
        }

        for (FcmToken fcmToken : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(fcmToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();

                FirebaseMessaging.getInstance().send(message);
                log.info("Sent notification to user {}, token {}", user.getId(), fcmToken.getToken());
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send FCM message to token: {}", fcmToken.getToken(), e);
                if (e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED ||
                        e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT) {
                    fcmTokenRepository.delete(fcmToken);
                }
            }
        }
    }
}
