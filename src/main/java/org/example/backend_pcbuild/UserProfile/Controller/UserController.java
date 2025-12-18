package org.example.backend_pcbuild.UserProfile.Controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.CategoryDTO;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.SavedPost;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.UserProfile.DTO.SavedPostDTO;
import org.example.backend_pcbuild.UserProfile.DTO.UserPostsDTO;
import org.example.backend_pcbuild.UserProfile.Repository.SavedPostRepository;
import org.example.backend_pcbuild.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/community")
public class UserController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SavedPostRepository savedPostRepository;

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
    @Transactional
    public ResponseEntity<List<UserPostsDTO>> getMyPosts() {
        User user = getAuthenticatedUser();

        List<Post> myPosts = postRepository.findByUserId(user.getId());

        List<UserPostsDTO> dtoList = myPosts.stream().map(post -> {
            Long imgId = (post.getImages() != null && !post.getImages().isEmpty())
                    ? post.getImages().iterator().next().getId()
                    : null;

            return UserPostsDTO.builder()
                    .id(post.getId())
                    .userId(post.getUser().getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .category(new CategoryDTO(post.getCategory().getId(), post.getCategory().getName()))
                    .imageId(imgId)
                    .createdAt(post.getCreatedAt())
                    .build();
        }).toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/posts/user/{username}")
    @Transactional
    public ResponseEntity<List<UserPostsDTO>> getPostsByUsername(@PathVariable String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with username: " + username));

        List<Post> userPosts = postRepository.findByUserId(user.getId());

        List<UserPostsDTO> dtoList = userPosts.stream()
                .map(post -> {
                    Long imgId = (post.getImages() != null && !post.getImages().isEmpty())
                            ? post.getImages().iterator().next().getId()
                            : null;

                    return UserPostsDTO.builder()
                            .id(post.getId())
                            .userId(user.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .category(CategoryDTO.builder()
                                    .id(post.getCategory().getId())
                                    .name(post.getCategory().getName())
                                    .build())
                            .imageId(imgId)
                            .createdAt(post.getCreatedAt())
                            .build();
                })
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/posts/saved/{username}")
    @Transactional
    public ResponseEntity<List<SavedPostDTO>> getSavedPostsByUsername(@PathVariable String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with username: " + username));

        List<SavedPost> savedEntities = savedPostRepository.findByUserId(user.getId());

        List<SavedPostDTO> savedPosts = savedEntities.stream()
                .map(savedPost -> {
                    Long imgId = (savedPost.getPost().getImages() != null && !savedPost.getPost().getImages().isEmpty())
                            ? savedPost.getPost().getImages().iterator().next().getId()
                            : null;

                    return SavedPostDTO.builder()
                            .id(savedPost.getId())
                            .postId(savedPost.getPost().getId())
                            .userId(savedPost.getUser().getId())
                            .title(savedPost.getPost().getTitle())
                            .content(savedPost.getPost().getContent())
                            .authorName(savedPost.getPost().getUser().getUsername())
                            .category(CategoryDTO.builder()
                                    .id(savedPost.getPost().getCategory().getId())
                                    .name(savedPost.getPost().getCategory().getName())
                                    .build())
                            .imageId(imgId)
                            .createdAt(savedPost.getPost().getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(savedPosts);
    }

    @PostMapping("/posts/{postId}/save")
    public ResponseEntity<?> savePost(@PathVariable Long postId) {
        User user = getAuthenticatedUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (savedPostRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            return ResponseEntity.badRequest().body("Post is already saved.");
        }

        SavedPost savedPost = new SavedPost();
        savedPost.setUser(user);
        savedPost.setPost(post);
        savedPostRepository.save(savedPost);

        return ResponseEntity.ok("Post saved successfully.");
    }

    @DeleteMapping("/posts/{postId}/unsave")
    @Transactional
    public ResponseEntity<?> unsavePost(@PathVariable Long postId) {
        User user = getAuthenticatedUser();

        if (savedPostRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            savedPostRepository.deleteByUserIdAndPostId(user.getId(), postId);
            return ResponseEntity.ok("Post removed from saved.");
        }
        return ResponseEntity.badRequest().body("Post was not saved.");
    }

    @GetMapping("posts/{postId}/isSaved")
    public ResponseEntity<Boolean> isPostSaved(@PathVariable Long postId) {
        try {
            User user = getAuthenticatedUser();
            return ResponseEntity.ok(savedPostRepository.existsByUserIdAndPostId(user.getId(), postId));
        } catch (ResponseStatusException e) {
            return ResponseEntity.ok(false);
        }
    }
}