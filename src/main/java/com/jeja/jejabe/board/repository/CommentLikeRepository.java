package com.jeja.jejabe.board.repository;

import com.jeja.jejabe.board.domain.Comment;
import com.jeja.jejabe.board.domain.CommentLike;
import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByMemberAndComment(Member member, Comment comment);
    void deleteByMemberAndComment(Member member, Comment comment);
}
