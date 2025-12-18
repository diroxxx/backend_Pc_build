package org.example.backend_pcbuild.UserProfile.Controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import org.springframework.beans.factory.annotation.Autowired;
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
@NoArgsConstructor
@RequestMapping("/community")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SavedPostRepository savedPostRepository;

    @GetMapping("/posts/mine")
    @Transactional
    public ResponseEntity<List<UserPostsDTO>> getMyPosts() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();

        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

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
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(savedPosts);
    }
    @PostMapping("/posts/{postId}/save")
    public ResponseEntity<?> savePost(@PathVariable Long postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = ((UserDto) authentication.getPrincipal()).getEmail();
            User user = userRepository.findByEmail(email).orElseThrow();
            Post post = postRepository.findById(postId).orElseThrow();

            if (savedPostRepository.existsByUserIdAndPostId(user.getId(), postId)) {
                return ResponseEntity.badRequest().body("Post is already saved.");
            }
            SavedPost savedPost = new SavedPost();
            savedPost.setUser(user);
            savedPost.setPost(post);
            savedPostRepository.save(savedPost);
            return ResponseEntity.ok("Post saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/posts/{postId}/unsave")
    @Transactional
    public ResponseEntity<?> unsavePost(@PathVariable Long postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = ((UserDto) authentication.getPrincipal()).getEmail();
            User user = userRepository.findByEmail(email).orElseThrow();

            if (savedPostRepository.existsByUserIdAndPostId(user.getId(), postId)) {
                savedPostRepository.deleteByUserIdAndPostId(user.getId(), postId);
                return ResponseEntity.ok("Post removed from saved.");
            }
            return ResponseEntity.badRequest().body("Post was not saved.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("posts/{postId}/isSaved")
    public ResponseEntity<Boolean> isPostSaved(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(false);
        }
        UserDto principal = (UserDto) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail()).orElseThrow();
        return ResponseEntity.ok(savedPostRepository.existsByUserIdAndPostId(user.getId(), postId));
    }
}