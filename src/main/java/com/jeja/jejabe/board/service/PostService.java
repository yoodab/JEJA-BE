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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Long createPost(String boardKey, PostCreateRequestDto dto, Long authorMemberId) {
        Board board = boardRepository.findByBoardKey(boardKey).orElseThrow();
        Member author = memberRepository.findById(authorMemberId).orElseThrow();

        boolean finalIsPrivate = dto.isPrivate();
        if (board.isAlwaysSecret())
            finalIsPrivate = true;

        Post post = Post.builder()
                .board(board)
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(author)
                .isPrivate(finalIsPrivate)
                .isNotice(dto.isNotice())
                .build();
        if (dto.getAttachmentUrl() != null)
            post.setAttachment(dto.getAttachmentName(), dto.getAttachmentUrl());
        return postRepository.save(post).getPostId();
    }

    @Transactional(readOnly = true)
    public Page<PostSimpleResponseDto> getPostsByBoard(String boardKey, String keyword, Pageable pageable) {
        Board board = boardRepository.findByBoardKey(boardKey).orElseThrow();

        // 정렬 조건: 공지사항(isNotice=true) 우선, 그 다음 생성일 역순 (또는 요청된 정렬)
        // 사용자가 별도 정렬을 요청했더라도 공지사항은 항상 위에 떠야 한다면 아래 로직 사용
        Sort sort = Sort.by(Sort.Direction.DESC, "isNotice")
                .and(pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt"));

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return postRepository.findAllByBoardAndKeyword(board, keyword, sortedPageable)
                .map(PostSimpleResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<PostSimpleResponseDto> getPostsByBoard(Long boardKey, String keyword, Pageable pageable) {
        Board board = boardRepository.findById(boardKey).orElseThrow();

        // 정렬 조건: 공지사항(isNotice=true) 우선, 그 다음 생성일 역순 (또는 요청된 정렬)
        // 사용자가 별도 정렬을 요청했더라도 공지사항은 항상 위에 떠야 한다면 아래 로직 사용
        Sort sort = Sort.by(Sort.Direction.DESC, "isNotice")
                .and(pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt"));

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return postRepository.findAllByBoardAndKeyword(board, keyword, sortedPageable)
                .map(PostSimpleResponseDto::new);
    }

    @Transactional
    public PostDetailResponseDto getPostById(Long postId, UserDetailsImpl userDetails) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.increaseViewCount();

        PostDetailResponseDto response = new PostDetailResponseDto(post);

        Long memberId = (userDetails != null) ? userDetails.getUser().getMember().getId() : null;

        // 1. 게시글 좋아요 여부 확인
        if (memberId != null) {
            Member member = memberRepository.findById(memberId).orElseThrow();
            boolean isPostLiked = postLikeRepository.existsByMemberAndPost(member, post);
            response.setLiked(isPostLiked);

            // [추가] 수정/삭제 권한 설정
            boolean canEditDelete = boardGuard.canEditDeletePost(userDetails, postId);
            response.setCanEdit(canEditDelete);
            response.setCanDelete(canEditDelete);
        } else {
            response.setLiked(false);
            response.setCanEdit(false);
            response.setCanDelete(false);
        }

        // 2. 댓글 좋아요 목록 미리 가져오기 (최적화)
        Set<Long> likedCommentIds;
        if (memberId != null) {
            List<Long> ids = commentLikeRepository.findLikedCommentIdsByMemberAndPost(memberId, postId);
            likedCommentIds = new HashSet<>(ids);
        } else {
            likedCommentIds = Collections.emptySet();
        }

        // 3. 댓글 DTO 변환 시 likedCommentIds 전달
        List<CommentResponseDto> comments = commentRepository.findAllByPostAndParentIsNullOrderByCreatedAtAsc(post)
                .stream()
                .map(comment -> mapToCommentDto(comment, userDetails, likedCommentIds))
                .collect(Collectors.toList());

        response.setComments(comments);
        return response;
    }

    private CommentResponseDto mapToCommentDto(Comment comment, UserDetailsImpl userDetails,
                                               Set<Long> likedCommentIds) {
        CommentResponseDto dto = new CommentResponseDto(comment, likedCommentIds);

        // 권한 설정
        boolean canEditDelete = boardGuard.canEditDeleteComment(userDetails, comment.getCommentId());
        dto.setCanEdit(canEditDelete);
        dto.setCanDelete(canEditDelete);

        // 자식 댓글 재귀 매핑
        List<CommentResponseDto> children = comment.getChildren().stream()
                .map(child -> mapToCommentDto(child, userDetails, likedCommentIds))
                .collect(Collectors.toList());
        dto.setChildren(children);

        return dto;
    }

    public void updatePost(Long postId, PostUpdateRequestDto dto) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.update(dto.getTitle(), dto.getContent(), dto.isPrivate(), dto.isNotice());
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