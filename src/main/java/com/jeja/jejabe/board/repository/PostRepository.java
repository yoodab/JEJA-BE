package com.jeja.jejabe.board.repository;

import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.board.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByBoard(Board board, Pageable pageable);
}
