package com.jeja.jejabe.board.dto;

import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.board.domain.BoardAccessType;
import lombok.Getter;

@Getter
public class BoardResponseDto {
    private final Long boardId;
    private final String name;
    private final String boardKey;
    private final String description;
    private final BoardAccessType accessType;
    private final BoardAccessType writeAccessType;
    private boolean canWrite;

    public BoardResponseDto(Board board) {
        this.boardId = board.getBoardId();
        this.name = board.getName();
        this.boardKey = board.getBoardKey();
        this.description = board.getDescription();
        this.accessType = board.getAccessType();
        this.writeAccessType = board.getWriteAccessType();
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }
}