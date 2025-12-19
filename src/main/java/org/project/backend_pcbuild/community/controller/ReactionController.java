package org.project.backend_pcbuild.community.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.project.backend_pcbuild.community.service.ReactionService;
import org.project.backend_pcbuild.loginAndRegister.repository.UserRepository;
import org.project.backend_pcbuild.loginAndRegister.dto.UserDto;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
@RequestMapping("/community")
public class ReactionController {

    private final ReactionService reactionService;
    private final UserRepository userRepository;


    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<Integer> castVote(
            @PathVariable Long postId,
            @RequestParam String type
    ) {
        User user = getAuthenticatedUser();
        try {
            int newScore = reactionService.castPostVote(postId, user, type);
            return ResponseEntity.ok(newScore);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error casting vote");
        }
    }

    @GetMapping("/posts/{postId}/vote")
    public ResponseEntity<Integer> getScore(@PathVariable Long postId) {
        return ResponseEntity.ok(reactionService.getPostNetScore(postId));
    }

    @GetMapping("/posts/{postId}/vote/status")
    public ResponseEntity<String> getUserVoteStatus(@PathVariable Long postId) {
        try {
            User user = getAuthenticatedUser();
            return ResponseEntity.ok(reactionService.getPostVoteStatus(postId, user));
        } catch (ResponseStatusException e) {
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<Integer> castCommentVote(
            @PathVariable Long commentId,
            @RequestParam String type
    ) {
        User user = getAuthenticatedUser();
        try {
            int newScore = reactionService.castCommentVote(commentId, user, type);
            return ResponseEntity.ok(newScore);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error casting vote");
        }
    }

    @GetMapping("/comments/{commentId}/vote")
    public ResponseEntity<Integer> getCommentScore(@PathVariable Long commentId) {
        return ResponseEntity.ok(reactionService.getCommentNetScore(commentId));
    }

    @GetMapping("/comments/{commentId}/vote/status")
    public ResponseEntity<String> getCommentVoteStatus(@PathVariable Long commentId) {
        try {
            User user = getAuthenticatedUser();
            return ResponseEntity.ok(reactionService.getCommentVoteStatus(commentId, user));
        } catch (ResponseStatusException e) {
            return ResponseEntity.ok(null);
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        return userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));
    }
}