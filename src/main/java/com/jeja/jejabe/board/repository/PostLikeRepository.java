package com.jeja.jejabe.board.repository;

import com.jeja.jejabe.board.domain.Post;
import com.jeja.jejabe.board.domain.PostLike;
import com.jeja.jejabe.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMemberAndPost(Member member, Post post);
    void deleteByMemberAndPost(Member member, Post post);
    int countByPost(Post post);
}
