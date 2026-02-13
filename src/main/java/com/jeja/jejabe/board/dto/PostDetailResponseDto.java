package com.jeja.jejabe.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeja.jejabe.board.domain.Post;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostDetailResponseDto {
    private final Long postId;
    private final String title;
    private final String content;
    private final String authorName;
    private final String authorProfileImage;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final int likeCount;
    private final int commentCount;
    @JsonProperty("isPrivate")
    private final boolean isPrivate;

    @JsonProperty("isPrivate")
    public boolean isPrivate() {
        return isPrivate;
    }

    private final String attachmentUrl;
    private List<CommentResponseDto> comments;
    private boolean isLiked;
    private boolean canEdit;
    private boolean canDelete;

    public PostDetailResponseDto(Post post) {
        this(post, true);
    }

    public PostDetailResponseDto(Post post, boolean hasPermission) {
        this.postId = post.getPostId();
        this.title = hasPermission ? post.getTitle() : "비밀글입니다";
        this.content = hasPermission ? post.getContent() : "<p>비밀글입니다. 작성자와 관리자만 볼 수 있습니다.</p>";
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
        this.attachmentUrl = hasPermission ? post.getAttachmentUrl() : null;
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return "*";
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}