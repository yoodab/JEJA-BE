package com.jeja.jejabe.board.service;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.board.BoardGuard;
import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.board.domain.Comment;
import com.jeja.jejabe.board.domain.Post;
import com.jeja.jejabe.board.dto.*;
import com.jeja.jejabe.board.repository.*;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BoardGuard boardGuard;

    public Long createPost(String boardKey, PostCreateRequestDto dto, UserDetailsImpl userDetails) {
        Board board = boardRepository.findByBoardKey(boardKey).orElseThrow();
        Long authorMemberId = userDetails.getUser().getMember().getId();
        Member author = memberRepository.findById(authorMemberId).orElseThrow();

        log.info("Creating post - board: {}, isPrivate from DTO: {}, isNotice from DTO: {}",
                boardKey, dto.isPrivate(), dto.isNotice());

        boolean finalIsPrivate = dto.isPrivate();
        if (board.isAlwaysSecret())
            finalIsPrivate = true;

        // 공지 설정 권한 체크: 관리자/임원/목사님만 가능
        boolean finalIsNotice = dto.isNotice();
        if (finalIsNotice && !boardGuard.isAdmin(userDetails)) {
            finalIsNotice = false;
        }

        Post post = Post.builder()
                .board(board)
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(author)
                .isPrivate(finalIsPrivate)
                .isNotice(finalIsNotice)
                .build();
        if (dto.getAttachmentUrl() != null)
            post.setAttachment(dto.getAttachmentName(), dto.getAttachmentUrl());
        return postRepository.save(post).getPostId();
    }

    @Transactional(readOnly = true)
    public Page<PostSimpleResponseDto> getPostsByBoard(String boardKey, String keyword, Pageable pageable,
            UserDetailsImpl userDetails) {
        Board board = boardRepository.findByBoardKey(boardKey).orElseThrow();

        // 정렬 조건: 공지사항(isNotice=true) 우선, 그 다음 생성일 역순 (또는 요청된 정렬)
        Sort sort = Sort.by(Sort.Direction.DESC, "isNotice")
                .and(pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt"));

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        boolean canSeeSecret = boardGuard.isPastorOrAdmin(userDetails);
        Long currentMemberId = (userDetails != null && userDetails.getUser().getMember() != null)
                ? userDetails.getUser().getMember().getId()
                : -1L;

        return postRepository.findAllByBoardAndKeywordAndSecurity(board, keyword, sortedPageable)
                .map(post -> {
                    boolean hasPermission = canSeeSecret ||
                            (post.getAuthor() != null && post.getAuthor().getId().equals(currentMemberId)) ||
                            (!post.isPrivate() && !board.isAlwaysSecret());
                    return new PostSimpleResponseDto(post, hasPermission);
                });
    }

    @Transactional(readOnly = true)
    public Page<PostSimpleResponseDto> getPostsByBoard(Long boardId, String keyword, Pageable pageable,
            UserDetailsImpl userDetails) {
        Board board = boardRepository.findById(boardId).orElseThrow();

        // 정렬 조건: 공지사항(isNotice=true) 우선, 그 다음 생성일 역순 (또는 요청된 정렬)
        Sort sort = Sort.by(Sort.Direction.DESC, "isNotice")
                .and(pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt"));

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        boolean canSeeSecret = boardGuard.isPastorOrAdmin(userDetails);
        Long currentMemberId = (userDetails != null && userDetails.getUser().getMember() != null)
                ? userDetails.getUser().getMember().getId()
                : -1L;

        return postRepository.findAllByBoardAndKeywordAndSecurity(board, keyword, sortedPageable)
                .map(post -> {
                    boolean hasPermission = canSeeSecret ||
                            (post.getAuthor() != null && post.getAuthor().getId().equals(currentMemberId)) ||
                            (!post.isPrivate() && !board.isAlwaysSecret());
                    return new PostSimpleResponseDto(post, hasPermission);
                });
    }

    @Transactional
    public PostDetailResponseDto getPostById(Long postId, UserDetailsImpl userDetails, boolean incrementView) {
        Post post = postRepository.findById(postId).orElseThrow();

        // 게시판 조회 권한 체크
        if (!boardGuard.canReadBoard(userDetails, post.getBoard().getBoardKey())) {
            throw new AccessDeniedException("게시판 조회 권한이 없습니다.");
        }

        boolean hasPermission = boardGuard.canReadPost(userDetails, postId);

        if (incrementView) {
            post.increaseViewCount();
        }

        PostDetailResponseDto response = new PostDetailResponseDto(post, hasPermission);

        Long memberId = (userDetails != null && userDetails.getUser().getMember() != null)
                ? userDetails.getUser().getMember().getId()
                : null;

        // 권한이 있는 경우만 좋아요 여부 및 수정/삭제 권한 확인
        if (memberId != null && hasPermission) {
            Member member = memberRepository.findById(memberId).orElseThrow();
            boolean isPostLiked = postLikeRepository.existsByMemberAndPost(member, post);
            response.setLiked(isPostLiked);

            response.setCanEdit(boardGuard.canEditPost(userDetails, postId));
            response.setCanDelete(boardGuard.canDeletePost(userDetails, postId));
        } else {
            response.setLiked(false);
            response.setCanEdit(false);
            response.setCanDelete(false);
        }

        // 2. 댓글 좋아요 목록 미리 가져오기 (최적화) - 권한 있는 경우만
        final Set<Long> likedCommentIds;
        if (memberId != null && hasPermission) {
            List<Long> ids = commentLikeRepository.findLikedCommentIdsByMemberAndPost(memberId, postId);
            likedCommentIds = new HashSet<>(ids);
        } else {
            likedCommentIds = Collections.emptySet();
        }

        // 3. 댓글 DTO 변환 시 likedCommentIds 전달 - 권한 있는 경우만 댓글 표시
        List<CommentResponseDto> comments;
        if (hasPermission) {
            comments = commentRepository.findAllByPostAndParentIsNullOrderByCreatedAtAsc(post)
                    .stream()
                    .map(comment -> mapToCommentDto(comment, userDetails, likedCommentIds))
                    .collect(Collectors.toList());
        } else {
            comments = Collections.emptyList();
        }

        response.setComments(comments);
        return response;
    }

    private CommentResponseDto mapToCommentDto(Comment comment, UserDetailsImpl userDetails,
            Set<Long> likedCommentIds) {
        CommentResponseDto dto = new CommentResponseDto(comment, likedCommentIds);

        // 권한 설정
        dto.setCanEdit(boardGuard.canEditComment(userDetails, comment.getCommentId()));
        dto.setCanDelete(boardGuard.canDeleteComment(userDetails, comment.getCommentId()));

        // 자식 댓글 재귀 매핑
        List<CommentResponseDto> children = comment.getChildren().stream()
                .map(child -> mapToCommentDto(child, userDetails, likedCommentIds))
                .collect(Collectors.toList());
        dto.setChildren(children);

        return dto;
    }

    public void updatePost(Long postId, PostUpdateRequestDto dto, UserDetailsImpl userDetails) {
        Post post = postRepository.findById(postId).orElseThrow();

        boolean finalIsNotice = dto.isNotice();
        // 권한이 없는데 공지로 설정하려고 하면 기존 값 유지 또는 false
        if (finalIsNotice != post.isNotice() && !boardGuard.isAdmin(userDetails)) {
            finalIsNotice = post.isNotice();
        }

        post.update(dto.getTitle(), dto.getContent(), dto.isPrivate(), finalIsNotice);
        if (dto.getAttachmentUrl() != null)
            post.setAttachment(dto.getAttachmentName(), dto.getAttachmentUrl());
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        postRepository.delete(post);
    }

    public void togglePostNotice(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.toggleNotice();
    }
}