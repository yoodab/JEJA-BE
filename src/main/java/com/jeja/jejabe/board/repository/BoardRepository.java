package com.jeja.jejabe.board.repository;

import com.jeja.jejabe.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByBoardKey(String boardKey);
}