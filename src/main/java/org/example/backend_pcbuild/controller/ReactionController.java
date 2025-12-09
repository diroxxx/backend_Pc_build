package org.example.backend_pcbuild.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.PostComment;
import org.example.backend_pcbuild.Community.Models.Reaction;
import org.example.backend_pcbuild.Community.Models.ReactionComment;
import org.example.backend_pcbuild.Community.Repository.PostCommentRepository;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.Community.Repository.ReactionCommentRepository;
import org.example.backend_pcbuild.Community.Repository.ReactionRepository;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.models.User;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/community")
public class ReactionController {

    private final ReactionRepository reactionRepository;
    private final ReactionCommentRepository reactionCommentRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final UserRepository userRepository;

    @PostMapping("/posts/{postId}/vote")
    @Transactional
    public ResponseEntity<Integer> castVote(
            @PathVariable Long postId,
            @RequestParam String type
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        Long userId = user.getId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));


        final boolean isLike;
        if ("upvote".equalsIgnoreCase(type)) {
            isLike = true;
        } else if ("downvote".equalsIgnoreCase(type)) {
            isLike = false;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vote type. Must be 'upvote' or 'downvote'.");
        }

        try {
            Optional<Reaction> existingVoteOpt = reactionRepository.findByPostIdAndUserId(postId, userId);

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

            return ResponseEntity.ok(getPostNetScore(postId));

        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error during vote: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{postId}/vote")
    public ResponseEntity<Integer> getScore(@PathVariable Long postId) {
        try {
            int score = getPostNetScore(postId);
            return ResponseEntity.ok(score);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving score.");
        }
    }

    @GetMapping("/posts/{postId}/vote/status")
    public ResponseEntity<String> getUserVoteStatus(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(null);
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        Long userId = user.getId();

        Optional<Reaction> existingVoteOpt = reactionRepository.findByPostIdAndUserId(postId, userId);

        if (existingVoteOpt.isPresent()) {
            return ResponseEntity.ok(existingVoteOpt.get().getLikeReaction() ? "upvote" : "downvote");
        } else {
            return ResponseEntity.ok(null);
        }
    }

    private int getPostNetScore(Long postId) {
        long likes = reactionRepository.countByPostIdAndLikeReaction(postId, true);
        long dislikes = reactionRepository.countByPostIdAndLikeReaction(postId, false);
        return (int) (likes - dislikes);
    }

    @PostMapping("/comments/{commentId}/vote")
    @Transactional
    public ResponseEntity<Integer> castCommentVote(@PathVariable Long commentId, @RequestParam String type) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        boolean isLike;
        if ("upvote".equalsIgnoreCase(type)) {
            isLike = true;
        } else if ("downvote".equalsIgnoreCase(type)) {
            isLike = false;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vote type");
        }

        try {
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

            long likes = reactionCommentRepository.countByCommentIdAndLikeReaction(commentId, true);
            long dislikes = reactionCommentRepository.countByCommentIdAndLikeReaction(commentId, false);
            return ResponseEntity.ok((int) (likes - dislikes));

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @GetMapping("/comments/{commentId}/vote")
    public ResponseEntity<Integer> getCommentScore(@PathVariable Long commentId) {
        try {
            long likes = reactionCommentRepository.countByCommentIdAndLikeReaction(commentId, true);
            long dislikes = reactionCommentRepository.countByCommentIdAndLikeReaction(commentId, false);
            return ResponseEntity.ok((int) (likes - dislikes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/comments/{commentId}/vote/status")
    public ResponseEntity<String> getCommentVoteStatus(@PathVariable Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(null);
        }

        try {
            UserDto principal = (UserDto) authentication.getPrincipal();
            User user = userRepository.findByEmail(principal.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<ReactionComment> vote = reactionCommentRepository.findByCommentIdAndUserId(commentId, user.getId());

            if (vote.isPresent()) {
                return ResponseEntity.ok(vote.get().getLikeReaction() ? "upvote" : "downvote");
            } else {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}