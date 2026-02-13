package com.jeja.jejabe.board.repository;

import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.board.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
        Page<Post> findAllByBoard(Board board, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.board = :board " +
                        "AND (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
        Page<Post> findAllByBoardAndKeywordAndSecurity(
                        @Param("board") Board board,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}
