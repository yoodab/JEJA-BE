package com.jeja.jejabe.board;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.board.domain.Board;
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

    // 게시판 읽기 권한
    @Transactional(readOnly = true)
    public boolean canReadBoard(UserDetailsImpl userDetails, String boardKey) {
        Board board = boardRepository.findByBoardKey(boardKey).orElse(null);
        if (board == null) return true; // 서비스에서 404 처리

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

    // 게시판 글쓰기 권한 (읽기 가능하면 쓰기도 가능하다고 가정)
    @Transactional(readOnly = true)
    public boolean canWritePost(UserDetailsImpl userDetails, String boardKey) {
        if (userDetails == null) return false;
        return canReadBoard(userDetails, boardKey);
    }

    // 게시글 상세 조회 (비밀글 체크)
    @Transactional(readOnly = true)
    public boolean canReadPost(UserDetailsImpl userDetails, Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return true;

        if (!canReadBoard(userDetails, post.getBoard().getBoardKey())) return false;

        if (post.isPrivate()) {
            if (isAdmin(userDetails)) return true;
            Member member = getMember(userDetails);
            if (member == null) return false;
            return post.getAuthor().getId().equals(member.getId());
        }
        return true;
    }

    // 게시글 수정/삭제
    @Transactional(readOnly = true)
    public boolean canEditDeletePost(UserDetailsImpl userDetails, Long postId) {
        if (userDetails == null) return false;
        if (isAdmin(userDetails)) return true;
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return true;
        return post.getAuthor().getId().equals(getMember(userDetails).getId());
    }

    // 댓글 삭제
    @Transactional(readOnly = true)
    public boolean canDeleteComment(UserDetailsImpl userDetails, Long commentId) {
        if (userDetails == null) return false;
        if (isAdmin(userDetails)) return true;
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return true;
        return comment.getAuthor().getId().equals(getMember(userDetails).getId());
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
}