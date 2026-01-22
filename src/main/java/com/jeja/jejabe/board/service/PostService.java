package com.jeja.jejabe.board.service;

import com.jeja.jejabe.board.domain.Board;
import com.jeja.jejabe.board.domain.Post;
import com.jeja.jejabe.board.dto.*;
import com.jeja.jejabe.board.repository.BoardRepository;
import com.jeja.jejabe.board.repository.CommentRepository;
import com.jeja.jejabe.board.repository.PostRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Transactional
public class PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    public Long createPost(String boardKey, PostCreateRequestDto dto, Long authorMemberId) {
        Board board = boardRepository.findByBoardKey(boardKey).orElseThrow();
        Member author = memberRepository.findById(authorMemberId).orElseThrow();

        boolean finalIsPrivate = dto.isPrivate();
        if (board.isAlwaysSecret()) finalIsPrivate = true;

        Post post = Post.builder()
                .board(board)
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(author)
                .isPrivate(finalIsPrivate)
                .isNotice(dto.isNotice())
                .build();
        if (dto.getAttachmentUrl() != null) post.setAttachment(dto.getAttachmentName(), dto.getAttachmentUrl());
        return postRepository.save(post).getPostId();
    }

    @Transactional(readOnly = true)
    public Page<PostSimpleResponseDto> getPostsByBoard(String boardKey, Pageable pageable) {
        Board board = boardRepository.findByBoardKey(boardKey).orElseThrow();
        return postRepository.findAllByBoard(board, pageable).map(PostSimpleResponseDto::new);
    }

    @Transactional(readOnly = true)
    public PostDetailResponseDto getPostById(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        // 상세 조회 시 댓글도 함께 반환
        PostDetailResponseDto response = new PostDetailResponseDto(post);
        List<CommentResponseDto> comments = commentRepository.findAllByPostAndParentIsNullOrderByCreatedAtAsc(post)
                .stream().map(CommentResponseDto::new).collect(Collectors.toList());
        response.setComments(comments);
        return response;
    }

    public void updatePost(Long postId, PostUpdateRequestDto dto) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.update(dto.getTitle(), dto.getContent(), dto.isPrivate(),dto.isNotice());
        if(dto.getAttachmentUrl() != null) post.setAttachment(dto.getAttachmentName(), dto.getAttachmentUrl());
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        postRepository.delete(post);
    }
}