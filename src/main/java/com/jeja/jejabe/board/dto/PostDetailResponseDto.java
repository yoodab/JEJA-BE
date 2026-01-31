package com.jeja.jejabe.board.dto;

import com.jeja.jejabe.board.domain.Post;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class PostDetailResponseDto {
    private final Long postId;
    private final String title;
    private final String content;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final int likeCount;
    private final int commentCount;
    private final boolean isPrivate;
    private final String attachmentUrl;
    private List<CommentResponseDto> comments;
    private boolean isLiked;

    public PostDetailResponseDto(Post post) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorName = post.getAuthor() != null ? post.getAuthor().getName() : "탈퇴한 사용자";
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.isPrivate = post.isPrivate();
        this.attachmentUrl = post.getAttachmentUrl();
    }
}