package com.jeja.jejabe.board.dto;

import com.jeja.jejabe.board.domain.Comment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommentResponseDto {
    private final Long commentId;
    private final String content;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final int likeCount;
    private final boolean isDeleted;
    private final List<CommentResponseDto> children;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getCommentId();
        this.content = comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent();
        this.authorName = comment.isDeleted() ? "(알수없음)" : comment.getAuthor().getName();
        this.createdAt = comment.getCreatedAt();
        this.likeCount = comment.getLikeCount();
        this.isDeleted = comment.isDeleted();
        this.children = comment.getChildren().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
}
