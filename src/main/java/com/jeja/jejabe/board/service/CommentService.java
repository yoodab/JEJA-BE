package com.jeja.jejabe.board.service;

import com.jeja.jejabe.board.domain.Comment;
import com.jeja.jejabe.board.domain.Post;
import com.jeja.jejabe.board.dto.CommentRequestDto;
import com.jeja.jejabe.board.repository.CommentRepository;
import com.jeja.jejabe.board.repository.PostRepository;
import com.jeja.jejabe.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public Long createComment(Long postId, CommentRequestDto dto, Long memberId) {
        Post post = postRepository.findById(postId).orElseThrow();
        Comment parent = null;
        if (dto.getParentId() != null) parent = commentRepository.findById(dto.getParentId()).orElseThrow();

        Comment comment = Comment.builder()
                .post(post)
                .author(memberRepository.findById(memberId).orElseThrow())
                .content(dto.getContent())
                .parent(parent).build();
        commentRepository.save(comment);
        post.updateCommentCount(post.getCommentCount() + 1);
        return comment.getCommentId();
    }

    public void updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        comment.update(content);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        if (comment.getChildren().size() > 0) {
            comment.delete(); // soft delete
        } else {
            commentRepository.delete(comment);
            comment.getPost().updateCommentCount(comment.getPost().getCommentCount() - 1);
        }
    }
}
