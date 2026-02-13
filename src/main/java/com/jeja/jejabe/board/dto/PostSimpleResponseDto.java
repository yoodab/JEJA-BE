package com.jeja.jejabe.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeja.jejabe.board.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostSimpleResponseDto {
    private final Long postId;
    private final String title;
    private final String authorName;
    private final String authorProfileImage;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final int likeCount;
    private final int commentCount;
    @JsonProperty("isPrivate")
    private final boolean isPrivate;
    @JsonProperty("isNotice")
    private final boolean isNotice;

    @JsonProperty("isPrivate")
    public boolean isPrivate() {
        return isPrivate;
    }

    @JsonProperty("isNotice")
    public boolean isNotice() {
        return isNotice;
    }

    public PostSimpleResponseDto(Post post) {
        this(post, true);
    }

    public PostSimpleResponseDto(Post post, boolean hasPermission) {
        this.postId = post.getPostId();
        this.title = hasPermission ? post.getTitle() : "비밀글입니다";
        this.authorName = (hasPermission || post.getAuthor() == null) 
                ? (post.getAuthor() != null ? post.getAuthor().getName() : "탈퇴한 사용자")
                : maskName(post.getAuthor().getName());
        this.authorProfileImage = (hasPermission && post.getAuthor() != null) 
                ? post.getAuthor().getUser().getProfileImageUrl() 
                : null;
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.isPrivate = post.isPrivate();
        this.isNotice = post.isNotice();
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return "*";
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}