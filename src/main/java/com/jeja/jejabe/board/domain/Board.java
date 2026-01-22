package com.jeja.jejabe.board.domain;

import com.jeja.jejabe.club.Club;
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
public class Board {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String boardKey;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardAccessType accessType = BoardAccessType.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(nullable = false)
    private boolean isAlwaysSecret = false;

    @Column(nullable = false)
    private int orderIndex; // 메뉴 순서 제어용

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @Builder
    public Board(String name, String boardKey, String description, BoardAccessType accessType, Club club, boolean isAlwaysSecret,int orderIndex) {
        this.name = name;
        this.boardKey = boardKey;
        this.description = description;
        this.accessType = accessType != null ? accessType : BoardAccessType.PUBLIC;
        this.club = club;
        this.orderIndex = orderIndex;
        this.isAlwaysSecret = isAlwaysSecret;
    }

    public void update(String name, String description, BoardAccessType accessType, Club club, Boolean isAlwaysSecret) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (accessType != null) this.accessType = accessType;
        this.club = club; // null 허용 (클럽 해제 시)
        if (isAlwaysSecret != null) this.isAlwaysSecret = isAlwaysSecret;
    }
}