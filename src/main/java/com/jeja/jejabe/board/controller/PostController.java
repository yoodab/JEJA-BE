package com.jeja.jejabe.board.controller;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.board.service.PostService;
import com.jeja.jejabe.board.dto.PostCreateRequestDto;
import com.jeja.jejabe.board.dto.PostUpdateRequestDto;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/boards/{boardKey}/posts")
    @PreAuthorize("@boardGuard.canReadBoard(principal, #boardKey)")
    public ResponseEntity<?> getPosts(@PathVariable String boardKey, Pageable pageable) {
        return ResponseEntity.ok(ApiResponseForm.success(postService.getPostsByBoard(boardKey, pageable), "목록 조회 성공"));
    }

    @PostMapping("/boards/{boardKey}/posts")
    @PreAuthorize("@boardGuard.canWritePost(principal, #boardKey)")
    public ResponseEntity<?> createPost(@PathVariable String boardKey, @RequestBody PostCreateRequestDto dto, @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(ApiResponseForm.success(postService.createPost(boardKey, dto, user.getUser().getMember().getId()), "등록 완료"));
    }

    @GetMapping("/posts/{postId}")
    @PreAuthorize("@boardGuard.canReadPost(principal, #postId)")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponseForm.success(postService.getPostById(postId), "상세 조회 성공"));
    }

    @PatchMapping("/posts/{postId}")
    @PreAuthorize("@boardGuard.canEditDeletePost(principal, #postId)")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody PostUpdateRequestDto dto) {
        postService.updatePost(postId, dto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "수정 완료"));
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("@boardGuard.canEditDeletePost(principal, #postId)")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "삭제 완료"));
    }
}