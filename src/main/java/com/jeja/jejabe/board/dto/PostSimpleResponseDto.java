package com.jeja.jejabe.board.dto;

import com.jeja.jejabe.board.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostSimpleResponseDto {
    private final Long postId;
    private final String title;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final int likeCount;
    private final int commentCount;
    private final boolean isPrivate;
    private final boolean isNotice;

    public PostSimpleResponseDto(Post post) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.authorName = post.getAuthor() != null ? post.getAuthor().getName() : "탈퇴한 사용자";
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.isPrivate = post.isPrivate();
        this.isNotice = post.isNotice();
    }
}