package com.jeja.jejabe.board.service;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.board.BoardGuard;
import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.board.domain.BoardAccessType;
import com.jeja.jejabe.board.dto.BoardCreateRequestDto;
import com.jeja.jejabe.board.dto.BoardResponseDto;
import com.jeja.jejabe.board.dto.BoardUpdateRequestDto;
import com.jeja.jejabe.board.repository.BoardRepository;
import com.jeja.jejabe.club.Club;
import com.jeja.jejabe.club.ClubRepository;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;
    private final ClubRepository clubRepository;
    private final BoardGuard boardGuard;

    @CacheEvict(value = "boardList", allEntries = true)
    public Long createBoard(BoardCreateRequestDto dto) {
        if (boardRepository.findByBoardKey(dto.getBoardKey()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 게시판 키입니다.");
        }
        Club linkedClub = null;
        if (dto.getClubId() != null) {
            linkedClub = clubRepository.findById(dto.getClubId()).orElseThrow();
        }
        Board board = Board.builder()
                .name(dto.getName())
                .boardKey(dto.getBoardKey())
                .description(dto.getDescription())
                .accessType(dto.getAccessType())
                .writeAccessType(dto.getWriteAccessType())
                .club(linkedClub)
                .isAlwaysSecret(dto.isAlwaysSecret())
                .build();
        return boardRepository.save(board).getBoardId();
    }

    @CacheEvict(value = "boardList", allEntries = true)
    public void updateBoard(Long boardId, BoardUpdateRequestDto dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.BOARD_NOT_FOUND));
        Club linkedClub = board.getClub();
        if (dto.getClubId() != null) {
            linkedClub = clubRepository.findById(dto.getClubId()).orElseThrow();
        }
        board.update(dto.getName(), dto.getDescription(), dto.getAccessType(), dto.getWriteAccessType(), linkedClub,
                dto.getIsAlwaysSecret());
    }

    @CacheEvict(value = "boardList", allEntries = true)
    public void deleteBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.BOARD_NOT_FOUND));
        boardRepository.delete(board);
    }

    // ★★★ 전체 목록 조회 (캐시 + 필터링)
    @Transactional(readOnly = true)
    public List<BoardResponseDto> getAllBoards() {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        boolean isLoggedIn = (userDetails != null);
        boolean isAdmin = isLoggedIn && (userDetails.getUser().getUserRole() == UserRole.ROLE_ADMIN);

        // 캐시된 전체 목록 가져오기 (실제론 별도 메소드로 분리하여 @Cacheable 적용 권장)
        List<Board> allBoards = boardRepository.findAll();

        return allBoards.stream()
                .filter(board -> {
                    if (isAdmin)
                        return true;
                    // ★ 중요: 일반 조회에서 CLUB 게시판은 숨김
                    if (board.getAccessType() == BoardAccessType.CLUB)
                        return false;
                    if (board.getAccessType() == BoardAccessType.MEMBER)
                        return isLoggedIn;
                    return true; // PUBLIC
                })
                .map(board -> {
                    BoardResponseDto dto = new BoardResponseDto(board);
                    dto.setCanWrite(boardGuard.canWritePost(userDetails, board));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private UserDetailsImpl getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) auth.getPrincipal();
        }
        return null;
    }

    /**
     * [추가] 일반 게시판 목록 조회 (메인 화면용)
     * - 공지사항(notice), 자유게시판 등 클럽에 속하지 않은 게시판 반환
     */
    public List<BoardResponseDto> getGeneralBoards() {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        // Club이 null인 게시판만 가져옴 (순서: OrderIndex)
        List<Board> boards = boardRepository.findAllByClubIsNullOrderByOrderIndexAsc();

        return boards.stream()
                // 1. ADMIN 전용 게시판 제외
                .filter(board -> board.getAccessType() != BoardAccessType.ADMIN)
                // 2. [추가] 공지사항 게시판 제외 (boardKey가 "notice"인 경우)
                .filter(board -> !board.getBoardKey().equals("notice"))
                .map(board -> {
                    BoardResponseDto dto = new BoardResponseDto(board);
                    dto.setCanWrite(boardGuard.canWritePost(userDetails, board));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * [추가] 특정 클럽의 게시판 목록 조회
     * - 해당 클럽에 들어갔을 때 보여줄 탭(게시판) 목록
     */
    public List<BoardResponseDto> getClubBoards(Long clubId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클럽입니다."));

        List<Board> boards = boardRepository.findAllByClubOrderByOrderIndexAsc(club);

        return boards.stream()
                .map(board -> {
                    BoardResponseDto dto = new BoardResponseDto(board);
                    dto.setCanWrite(boardGuard.canWritePost(userDetails, board));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}