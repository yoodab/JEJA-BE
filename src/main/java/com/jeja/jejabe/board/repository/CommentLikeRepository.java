package com.jeja.jejabe.board.repository;

import com.jeja.jejabe.board.domain.Comment;
import com.jeja.jejabe.board.domain.CommentLike;
import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByMemberAndComment(Member member, Comment comment);
    void deleteByMemberAndComment(Member member, Comment comment);

    @Query("SELECT cl.comment.commentId FROM CommentLike cl " +
            "WHERE cl.member.id = :memberId AND cl.comment.post.postId = :postId")
    List<Long> findLikedCommentIdsByMemberAndPost(@Param("memberId") Long memberId,
                                                  @Param("postId") Long postId);
}
