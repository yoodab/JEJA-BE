package com.jeja.jejabe.board.repository;

import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.club.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByBoardKey(String boardKey);

    // [추가] 클럽에 속하지 않은 일반 게시판들 (공지사항, 자유게시판 등)
    List<Board> findAllByClubIsNullOrderByOrderIndexAsc();

    // [추가] 특정 클럽에 속한 게시판들
    List<Board> findAllByClubOrderByOrderIndexAsc(Club club);
}