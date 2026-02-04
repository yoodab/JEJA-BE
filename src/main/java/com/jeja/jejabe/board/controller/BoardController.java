package com.jeja.jejabe.board.controller;

import com.jeja.jejabe.board.service.BoardService;
import com.jeja.jejabe.board.dto.BoardCreateRequestDto;
import com.jeja.jejabe.board.dto.BoardUpdateRequestDto;
import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping("/admin/boards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBoard(@RequestBody BoardCreateRequestDto dto) {
        return ResponseEntity.ok(ApiResponseForm.success(boardService.createBoard(dto), "생성 완료"));
    }

    @PatchMapping("/admin/boards/{boardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBoard(@PathVariable Long boardId, @RequestBody BoardUpdateRequestDto dto) {
        boardService.updateBoard(boardId, dto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "수정 완료"));
    }

    @DeleteMapping("/admin/boards/{boardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "삭제 완료"));
    }

    @GetMapping("/boards")
    public ResponseEntity<?> getAllBoards() {
        return ResponseEntity.ok(ApiResponseForm.success(boardService.getAllBoards(), "조회 완료"));
    }

    @GetMapping("/boards/general")
    public ResponseEntity<?> getGeneralBoards() {
        return ResponseEntity.ok(ApiResponseForm.success(
                boardService.getGeneralBoards(),
                "일반 게시판 목록"
        ));
    }

    // [추가] 특정 클럽의 게시판 목록 조회
    // URL: GET /api/clubs/{clubId}/boards
    @GetMapping("/clubs/{clubId}/boards")
    public ResponseEntity<?> getClubBoards(@PathVariable Long clubId) {
        return ResponseEntity.ok(ApiResponseForm.success(
                boardService.getClubBoards(clubId),
                "클럽 게시판 목록"
        ));
    }
}