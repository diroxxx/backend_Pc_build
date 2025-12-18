package org.example.backend_pcbuild.UserProfile.Controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.UserProfile.DTO.SavedPostDTO;
import org.example.backend_pcbuild.UserProfile.DTO.UserPostsDTO;
import org.example.backend_pcbuild.UserProfile.Service.UserProfileService;
import org.example.backend_pcbuild.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/community")
public class UserController {

    private final UserProfileService userService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }
        UserDto principal = (UserDto) authentication.getPrincipal();
        return userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping("/posts/mine")
    public ResponseEntity<List<UserPostsDTO>> getMyPosts() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(userService.getUserPosts(user));
    }

    @GetMapping("/posts/user/{username}")
    public ResponseEntity<List<UserPostsDTO>> getPostsByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.getPostsByUsername(username));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/posts/saved/{username}")
    public ResponseEntity<List<SavedPostDTO>> getSavedPostsByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.getSavedPostsByUsername(username));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/posts/{postId}/save")
    public ResponseEntity<?> savePost(@PathVariable Long postId) {
        User user = getAuthenticatedUser();
        try {
            userService.savePost(postId, user);
            return ResponseEntity.ok("Post saved successfully.");
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/posts/{postId}/unsave")
    public ResponseEntity<?> unsavePost(@PathVariable Long postId) {
        User user = getAuthenticatedUser();
        try {
            userService.unsavePost(postId, user);
            return ResponseEntity.ok("Post removed from saved.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("posts/{postId}/isSaved")
    public ResponseEntity<Boolean> isPostSaved(@PathVariable Long postId) {
        try {
            User user = getAuthenticatedUser();
            return ResponseEntity.ok(userService.isPostSaved(postId, user));
        } catch (ResponseStatusException e) {
            return ResponseEntity.ok(false);
        }
    }
}