package org.project.backend_pcbuild.community.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.project.backend_pcbuild.community.model.Post;
import org.project.backend_pcbuild.community.model.PostComment;
import org.project.backend_pcbuild.community.repository.PostCommentRepository;
import org.project.backend_pcbuild.community.repository.PostRepository;
import org.project.backend_pcbuild.community.repository.ReactionCommentRepository;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class CommentService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReactionCommentRepository reactionCommentRepository;

    @Transactional
    public PostComment addComment(Long postId, String content, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        PostComment comment = new PostComment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }


    @Transactional
    public PostComment updateComment(Long commentId, String newContent, User currentUser) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the author of this comment.");
        }

        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty.");
        }

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = String.valueOf(currentUser.getRole()).equals("ADMIN");

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }

        if (comment.getReactions() != null && !comment.getReactions().isEmpty()) {
            reactionCommentRepository.deleteAll(comment.getReactions());
        }

        commentRepository.delete(comment);
    }
}