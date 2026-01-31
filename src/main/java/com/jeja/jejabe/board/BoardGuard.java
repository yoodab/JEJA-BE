package com.jeja.jejabe.board;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.board.domain.BoardAccessType;
import com.jeja.jejabe.board.domain.Comment;
import com.jeja.jejabe.board.domain.Post;
import com.jeja.jejabe.board.repository.BoardRepository;
import com.jeja.jejabe.board.repository.CommentRepository;
import com.jeja.jejabe.board.repository.PostRepository;
import com.jeja.jejabe.club.Club;
import com.jeja.jejabe.club.ClubMemberRepository;
import com.jeja.jejabe.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("boardGuard")
@RequiredArgsConstructor
public class BoardGuard {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ClubMemberRepository clubMemberRepository;

    // 게시판 읽기 권한 (Board 객체 직접 전달)
    @Transactional(readOnly = true)
    public boolean canReadBoard(UserDetailsImpl userDetails, Board board) {
        if (board == null) return true;

        if (isAdmin(userDetails)) return true;
        Member member = getMember(userDetails);

        switch (board.getAccessType()) {
            case PUBLIC:
                return true;
            case MEMBER:
                return member != null;
            case CLUB:
                if (member == null || board.getClub() == null) return false;
                return isClubMemberOrLeader(board.getClub(), member);
            case ADMIN:
                return false;
            default:
                return false;
        }
    }

    // 게시판 읽기 권한 (BoardKey로 조회)
    @Transactional(readOnly = true)
    public boolean canReadBoard(UserDetailsImpl userDetails, String boardKey) {
        Board board = boardRepository.findByBoardKey(boardKey).orElse(null);
        if (board == null)
            return true; // 서비스에서 404 처리

        return canReadBoard(userDetails, board);
    }

    // 게시판 글쓰기 권한 (Board 객체 직접 전달)
    @Transactional(readOnly = true)
    public boolean canWritePost(UserDetailsImpl userDetails, Board board) {
        if (board == null) return false;

        // 1. 관리자 프리패스
        if (isAdmin(userDetails)) return true;

        // 2. 기본적으로 읽기 권한이 있어야 쓰기도 가능하다고 가정
        if (!canReadBoard(userDetails, board)) return false;

        // 3. 쓰기 권한 체크
        Member member = getMember(userDetails);
        if (member == null) return false; // 글쓰기는 무조건 로그인 필요

        switch (board.getWriteAccessType()) {
            case PUBLIC:
            case MEMBER:
                return true; // 이미 위에서 member != null 체크함
            case CLUB:
                if (board.getClub() == null) return false;
                return isClubMemberOrLeader(board.getClub(), member);
            case ADMIN:
                return false; // 관리자는 위에서 처리됨
            default:
                return false;
        }
    }

    // 게시판 글쓰기 권한 (BoardKey로 조회)
    @Transactional(readOnly = true)
    public boolean canWritePost(UserDetailsImpl userDetails, String boardKey) {
        Board board = boardRepository.findByBoardKey(boardKey).orElse(null);
        return canWritePost(userDetails, board);
    }

    // 게시글 상세 조회 (비밀글 체크)
    @Transactional(readOnly = true)
    public boolean canReadPost(UserDetailsImpl userDetails, Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null)
            return true;

        if (!canReadBoard(userDetails, post.getBoard().getBoardKey()))
            return false;

        if (post.isPrivate()) {
            if (isAdmin(userDetails))
                return true;
            Member member = getMember(userDetails);
            if (member == null)
                return false;
            return post.getAuthor().getId().equals(member.getId());
        }
        return true;
    }

    // 게시글 수정/삭제
    @Transactional(readOnly = true)
    public boolean canEditDeletePost(UserDetailsImpl userDetails, Long postId) {
        if (userDetails == null)
            return false;
        if (isAdmin(userDetails))
            return true;
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null)
            return true;

        Member member = getMember(userDetails);

        // 1. 작성자 본인
        if (post.getAuthor().getId().equals(member.getId()))
            return true;

        // 2. 클럽 게시판의 경우 리더는 삭제 가능 (관리 권한)
        Board board = post.getBoard();
        if (board.getAccessType() == BoardAccessType.CLUB && board.getClub() != null) {
            return isClubMemberOrLeader(board.getClub(), member) &&
                    board.getClub().getLeader() != null &&
                    board.getClub().getLeader().getId().equals(member.getId());
        }

        return false;
    }

    // 댓글 삭제
    @Transactional(readOnly = true)
    public boolean canEditDeleteComment(UserDetailsImpl userDetails, Long commentId) {
        if (userDetails == null)
            return false;
        if (isAdmin(userDetails))
            return true;
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null)
            return true;

        Member member = getMember(userDetails);

        // 1. 작성자 본인
        if (comment.getAuthor().getId().equals(member.getId()))
            return true;

        // 2. 게시글 작성자도 댓글 삭제 가능하게 할지? (보통은 아님, 하지만 에타 등은 가능)
        // 여기서는 클럽 리더 권한만 추가
        Post post = comment.getPost();
        Board board = post.getBoard();
        if (board.getAccessType() == BoardAccessType.CLUB && board.getClub() != null) {
            return isClubMemberOrLeader(board.getClub(), member) &&
                    board.getClub().getLeader() != null &&
                    board.getClub().getLeader().getId().equals(member.getId());
        }

        return false;
    }

    private boolean isAdmin(UserDetailsImpl userDetails) {
        return userDetails != null && userDetails.getUser().getUserRole() == UserRole.ROLE_ADMIN;
    }

    private Member getMember(UserDetailsImpl userDetails) {
        return userDetails == null ? null : userDetails.getUser().getMember();
    }

    private boolean isClubMemberOrLeader(Club club, Member member) {
        boolean isMember = clubMemberRepository.existsByClubAndMember(club, member);
        boolean isLeader = club.getLeader() != null && club.getLeader().getId().equals(member.getId());
        return isMember || isLeader;
    }

    @Transactional(readOnly = true)
    public boolean canManagePost(UserDetailsImpl userDetails, Long postId) {
        if (userDetails == null)
            return false;
        if (isAdmin(userDetails))
            return true; // 시스템 관리자는 프리패스

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null)
            return false;

        Member member = getMember(userDetails);
        Board board = post.getBoard();

        // 클럽 게시판일 경우, 클럽의 리더인지 확인
        if (board.getAccessType() == BoardAccessType.CLUB && board.getClub() != null) {
            // Club 엔티티에 getLeader()가 있다고 가정
            return board.getClub().getLeader().getId().equals(member.getId());
        }

        // 일반적인 경우 작성자는 공지 권한이 없을 수도 있음 (정책에 따라 결정)
        // 여기서는 "작성자도 본인 글은 공지로 올릴 수 있다"고 가정한다면 아래 줄 추가
        // return post.getAuthor().getId().equals(member.getId());

        return false; // 그 외에는 권한 없음
    }
}