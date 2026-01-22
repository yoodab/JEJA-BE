package com.jeja.jejabe.board.controller;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.board.dto.CommentRequestDto;
import com.jeja.jejabe.board.service.CommentService;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated() and @boardGuard.canReadPost(principal, #postId)")
    public ResponseEntity<?> createComment(@PathVariable Long postId, @RequestBody CommentRequestDto dto, @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(ApiResponseForm.success(commentService.createComment(postId, dto, user.getUser().getMember().getId()), "댓글 등록"));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("@boardGuard.canDeleteComment(principal, #commentId)")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "댓글 삭제"));
    }
}
