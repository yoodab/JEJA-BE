package com.jeja.jejabe.board.controller;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.board.service.LikeService;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/posts/{postId}/likes")
    @PreAuthorize("isAuthenticated() and @boardGuard.canReadPost(principal, #postId)")
    public ResponseEntity<?> togglePostLike(@PathVariable Long postId, @AuthenticationPrincipal UserDetailsImpl user) {
        likeService.togglePostLike(postId, user.getUser().getMember().getId());
        return ResponseEntity.ok(ApiResponseForm.success(null, "좋아요"));
    }

    @PostMapping("/comments/{commentId}/likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleCommentLike(@PathVariable Long commentId, @AuthenticationPrincipal UserDetailsImpl user) {
        likeService.toggleCommentLike(commentId, user.getUser().getMember().getId());
        return ResponseEntity.ok(ApiResponseForm.success(null, "댓글 좋아요"));
    }
}
