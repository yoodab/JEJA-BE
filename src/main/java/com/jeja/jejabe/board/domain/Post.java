package com.jeja.jejabe.board.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false)
    private String title;

    @Lob @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_member_id")
    private Member author;

    @Column(nullable = false)
    private boolean isPrivate = false;

    @Column(nullable = false)
    private boolean isNotice = false;

    private String attachmentName;
    private String attachmentUrl;
    private int viewCount = 0;

    // 성능 최적화용 필드
    private int likeCount = 0;
    private int commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    // [추가] 게시글 삭제 시, 달려있는 '댓글'도 같이 삭제
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Post(Board board, String title, String content, Member author, boolean isPrivate ,boolean isNotice) {
        this.board = board;
        this.title = title;
        this.content = content;
        this.author = author;
        this.isPrivate = isPrivate;
        this.isNotice = isNotice;
    }

    public void setAttachment(String attachmentName, String attachmentUrl) {
        this.attachmentName = attachmentName;
        this.attachmentUrl = attachmentUrl;
    }

    public void update(String title, String content, boolean isPrivate, boolean isNotice) {
        this.title = title;
        this.content = content;
        this.isPrivate = isPrivate;
        this.isNotice = isNotice; // 추가
    }
    public void increaseViewCount() {
        this.viewCount++;
    }

    public void toggleNotice() {
        this.isNotice = !this.isNotice;
    }
    public void updateLikeCount(int count) { this.likeCount = count; }
    public void updateCommentCount(int count) { this.commentCount = count; }
}