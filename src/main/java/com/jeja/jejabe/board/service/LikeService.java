package com.jeja.jejabe.board.service;

import com.jeja.jejabe.board.domain.Comment;
import com.jeja.jejabe.board.domain.CommentLike;
import com.jeja.jejabe.board.domain.Post;
import com.jeja.jejabe.board.domain.PostLike;
import com.jeja.jejabe.board.repository.CommentLikeRepository;
import com.jeja.jejabe.board.repository.CommentRepository;
import com.jeja.jejabe.board.repository.PostLikeRepository;
import com.jeja.jejabe.board.repository.PostRepository;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    public void togglePostLike(Long postId, Long memberId) {
        Post post = postRepository.findById(postId).orElseThrow();
        Member member = memberRepository.findById(memberId).orElseThrow();
        if (postLikeRepository.existsByMemberAndPost(member, post)) {
            postLikeRepository.deleteByMemberAndPost(member, post);
            post.updateLikeCount(post.getLikeCount() - 1);
        } else {
            postLikeRepository.save(new PostLike(member, post));
            post.updateLikeCount(post.getLikeCount() + 1);
        }
    }

    public void toggleCommentLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        Member member = memberRepository.findById(memberId).orElseThrow();
        if (commentLikeRepository.existsByMemberAndComment(member, comment)) {
            commentLikeRepository.deleteByMemberAndComment(member, comment);
            comment.updateLikeCount(comment.getLikeCount() - 1);
        } else {
            commentLikeRepository.save(new CommentLike(member, comment));
            comment.updateLikeCount(comment.getLikeCount() + 1);
        }
    }
}
