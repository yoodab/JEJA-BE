package com.jeja.jejabe.board.dto;

import com.jeja.jejabe.board.domain.Board;
import lombok.Getter;

@Getter
public class BoardResponseDto {
    private final Long boardId;
    private final String name;
    private final String boardKey;
    private final String description;

    public BoardResponseDto(Board board) {
        this.boardId = board.getBoardId();
        this.name = board.getName();
        this.boardKey = board.getBoardKey();
        this.description = board.getDescription();
    }
}