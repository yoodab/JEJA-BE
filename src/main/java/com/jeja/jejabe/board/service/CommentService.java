package com.jeja.jejabe.board.service;

import com.jeja.jejabe.board.domain.Comment;
import com.jeja.jejabe.board.domain.Post;
import com.jeja.jejabe.board.dto.CommentRequestDto;
import com.jeja.jejabe.board.repository.CommentRepository;
import com.jeja.jejabe.board.repository.PostRepository;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.auth.UserRepository;
import com.jeja.jejabe.notification.service.FcmService;
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
    private final UserRepository userRepository;
    private final FcmService fcmService;

    public Long createComment(Long postId, CommentRequestDto dto, Long memberId) {
        Post post = postRepository.findById(postId).orElseThrow();
        Comment parent = null;
        if (dto.getParentId() != null) parent = commentRepository.findById(dto.getParentId()).orElseThrow();

        Member author = memberRepository.findById(memberId).orElseThrow();

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(dto.getContent())
                .parent(parent).build();
        commentRepository.save(comment);
        post.updateCommentCount(post.getCommentCount() + 1);

        // Send Notification
        sendCommentNotification(post, parent, author, dto.getContent());

        return comment.getCommentId();
    }

    private void sendCommentNotification(Post post, Comment parent, Member commenter, String content) {
        try {
            Member targetMember = null;
            String title = "";
            String body = "";

            if (parent != null) {
                targetMember = parent.getAuthor();
                title = "새 답글 알림";
                body = commenter.getName() + "님이 답글을 남겼습니다: " + content;
            } else {
                targetMember = post.getAuthor();
                title = "새 댓글 알림";
                body = commenter.getName() + "님이 댓글을 남겼습니다: " + content;
            }

            if (targetMember != null && !targetMember.getId().equals(commenter.getId())) {
                String finalTitle = title;
                String finalBody = body;
                userRepository.findByMember(targetMember).ifPresent(user ->
                        fcmService.sendNotificationToUser(user.getId(), finalTitle, finalBody)
                );
            }
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to send notification: " + e.getMessage());
        }
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
