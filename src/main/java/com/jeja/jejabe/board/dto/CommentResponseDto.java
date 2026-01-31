package com.jeja.jejabe.board.dto;

import com.jeja.jejabe.board.domain.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CommentResponseDto {
    private final Long commentId;
    private final String content;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final int likeCount;
    private final boolean isDeleted;

    private final boolean isLiked;
    @Setter
    private List<CommentResponseDto> children;
    @Setter
    private boolean canEdit;
    @Setter
    private boolean canDelete;

    public CommentResponseDto(Comment comment, Set<Long> likedCommentIds) {
        this.commentId = comment.getCommentId();
        this.content = comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent();
        this.authorName = comment.isDeleted() ? "(알수없음)" : comment.getAuthor().getName();
        this.createdAt = comment.getCreatedAt();
        this.likeCount = comment.getLikeCount();
        this.isDeleted = comment.isDeleted();

        this.isLiked = likedCommentIds != null && likedCommentIds.contains(comment.getCommentId());

        // children은 Service에서 재귀적으로 설정 (권한 체크 포함을 위해)
    }
}
