package org.project.backend_pcbuild.community.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.project.backend_pcbuild.community.model.*;
import org.project.backend_pcbuild.community.repository.*;
import org.project.backend_pcbuild.community.model.Post;
import org.project.backend_pcbuild.community.model.PostComment;
import org.project.backend_pcbuild.community.model.Reaction;
import org.project.backend_pcbuild.community.model.ReactionComment;
import org.project.backend_pcbuild.community.repository.PostCommentRepository;
import org.project.backend_pcbuild.community.repository.PostRepository;
import org.project.backend_pcbuild.community.repository.ReactionCommentRepository;
import org.project.backend_pcbuild.community.repository.ReactionRepository;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final ReactionCommentRepository reactionCommentRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;


    @Transactional
    public int castPostVote(Long postId, User user, String type) {
        boolean isLike = parseVoteType(type);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        Optional<Reaction> existingVoteOpt = reactionRepository.findByPostIdAndUserId(postId, user.getId());

        if (existingVoteOpt.isPresent()) {
            Reaction existingVote = existingVoteOpt.get();
            if (existingVote.getLikeReaction() == isLike) {
                reactionRepository.delete(existingVote);
            } else {
                existingVote.setLikeReaction(isLike);
                reactionRepository.save(existingVote);
            }
        } else {
            Reaction newVote = new Reaction();
            newVote.setPost(post);
            newVote.setUser(user);
            newVote.setLikeReaction(isLike);
            reactionRepository.save(newVote);
        }

        return getPostNetScore(postId);
    }

    public int getPostNetScore(Long postId) {
        long likes = reactionRepository.countByPostIdAndLikeReaction(postId, true);
        long dislikes = reactionRepository.countByPostIdAndLikeReaction(postId, false);
        return (int) (likes - dislikes);
    }

    public String getPostVoteStatus(Long postId, User user) {
        return reactionRepository.findByPostIdAndUserId(postId, user.getId())
                .map(reaction -> reaction.getLikeReaction() ? "upvote" : "downvote")
                .orElse(null);
    }

    @Transactional
    public int castCommentVote(Long commentId, User user, String type) {
        boolean isLike = parseVoteType(type);

        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        Optional<ReactionComment> existingVoteOpt = reactionCommentRepository.findByCommentIdAndUserId(commentId, user.getId());

        if (existingVoteOpt.isPresent()) {
            ReactionComment existingVote = existingVoteOpt.get();
            if (existingVote.getLikeReaction() == isLike) {
                reactionCommentRepository.delete(existingVote);
            } else {
                existingVote.setLikeReaction(isLike);
                reactionCommentRepository.save(existingVote);
            }
        } else {
            ReactionComment newVote = new ReactionComment();
            newVote.setComment(comment);
            newVote.setUser(user);
            newVote.setLikeReaction(isLike);
            reactionCommentRepository.save(newVote);
        }

        return getCommentNetScore(commentId);
    }

    public int getCommentNetScore(Long commentId) {
        long likes = reactionCommentRepository.countByCommentIdAndLikeReaction(commentId, true);
        long dislikes = reactionCommentRepository.countByCommentIdAndLikeReaction(commentId, false);
        return (int) (likes - dislikes);
    }

    public String getCommentVoteStatus(Long commentId, User user) {
        return reactionCommentRepository.findByCommentIdAndUserId(commentId, user.getId())
                .map(reaction -> reaction.getLikeReaction() ? "upvote" : "downvote")
                .orElse(null);
    }

    private boolean parseVoteType(String type) {
        if ("upvote".equalsIgnoreCase(type)) {
            return true;
        } else if ("downvote".equalsIgnoreCase(type)) {
            return false;
        } else {
            throw new IllegalArgumentException("Invalid vote type. Must be 'upvote' or 'downvote'.");
        }
    }
}