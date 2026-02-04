package com.jeja.jejabe.notification.controller;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.notification.dto.FcmTokenRequestDto;
import com.jeja.jejabe.notification.dto.NotificationRequestDto;
import com.jeja.jejabe.notification.service.FcmService;
import com.jeja.jejabe.notification.dto.NotificationSendRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final FcmService fcmService;

    @PostMapping("/token")
    public ApiResponseForm<Void> registerToken(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody FcmTokenRequestDto requestDto) {
        fcmService.saveToken(userDetails.getUser(), requestDto.getToken());
        return ApiResponseForm.success(null);
    }

    @DeleteMapping("/token")
    public ApiResponseForm<Void> removeToken(@RequestBody FcmTokenRequestDto requestDto) {
        fcmService.deleteToken(requestDto.getToken());
        return ApiResponseForm.success(null);
    }

    // Test endpoint (admin only ideally, but public for now for testing)
    @PostMapping("/send")
    public ApiResponseForm<Void> sendNotification(@RequestBody NotificationRequestDto requestDto) {
        fcmService.sendNotificationToUser(requestDto.getTargetUserId(), requestDto.getTitle(), requestDto.getBody());
        return ApiResponseForm.success(null);
    }

    @PostMapping("/admin/send")
    public ApiResponseForm<Void> sendAdminNotification(@RequestBody NotificationSendRequestDto requestDto) {
        fcmService.sendNotification(requestDto);
        return ApiResponseForm.success(null);
    }
}
