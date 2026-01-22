package com.jeja.jejabe.board.repository;

import com.jeja.jejabe.board.domain.Comment;
import com.jeja.jejabe.board.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 게시글의 댓글 목록 (부모 댓글만 조회하고 자식은 엔티티 관계나 별도 로직으로 처리)
    // 여기서는 간단하게 부모가 없는 최상위 댓글만 가져와서 계층구조로 변환하는 방식을 씁니다.
    List<Comment> findAllByPostAndParentIsNullOrderByCreatedAtAsc(Post post);
}
