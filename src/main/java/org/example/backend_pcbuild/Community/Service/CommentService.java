package org.example.backend_pcbuild.Community.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.PostCommentDTO;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.PostComment;
import org.example.backend_pcbuild.Community.Repository.PostCommentRepository;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.Community.Repository.ReactionCommentRepository;
import org.example.backend_pcbuild.models.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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