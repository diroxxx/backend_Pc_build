package org.example.backend_pcbuild.UserProfile;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.CategoryDTO;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.SavedPost;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
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
    public ResponseEntity<List<UserPostsDTO>> getMyPosts() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();

        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        List<Post> myPosts = postRepository.findByUserId(user.getId());

        // Mapowanie Post -> UserPostsDTO
        List<UserPostsDTO> dtoList = myPosts.stream().map(post -> {
            UserPostsDTO dto = new UserPostsDTO(
                    post.getId(),
                    post.getUser().getId(),
                    post.getTitle(),
                    post.getContent(),
                    new CategoryDTO(post.getCategory().getId(), post.getCategory().getName())
            );
            dto.setId(post.getId());
            dto.setUserId(post.getUser().getId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            dto.setCategory(new CategoryDTO(post.getCategory().getId(), post.getCategory().getName()));
            return dto;
        }).toList();


        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/posts/user/{username}")
    public ResponseEntity<List<UserPostsDTO>> getPostsByUsername(@PathVariable String username) {

        // Pobranie użytkownika po username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with username: " + username));

        // Pobranie postów użytkownika
        List<Post> userPosts = postRepository.findByUserId(user.getId());

        // Mapowanie Post -> UserPostsDTO z użyciem buildera
        List<UserPostsDTO> dtoList = userPosts.stream()
                .map(post -> UserPostsDTO.builder()
                        .id(post.getId())
                        .userId(user.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .category(CategoryDTO.builder()
                                .id(post.getCategory().getId())
                                .name(post.getCategory().getName())
                                .build())
                        .build())
                .toList();

        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/posts/saved/{username}")
    public ResponseEntity<List<SavedPostDTO>> getSavedPostsByUsername(@PathVariable String username) {


        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with username: " + username));


        List<SavedPost> savedEntities = savedPostRepository.findByUserId(user.getId());


        List<SavedPostDTO> savedPosts = savedEntities.stream()
                .map(savedPost -> SavedPostDTO.builder()
                        .id(savedPost.getId()) // ID samego zapisu (jeśli potrzebne)
                        .postId(savedPost.getPost().getId()) // ID zapisanego posta
                        .userId(savedPost.getUser().getId()) // ID użytkownika (opcjonalne, bo znamy go z URL)
                        .title(savedPost.getPost().getTitle())
                        .content(savedPost.getPost().getContent())
                        .category(CategoryDTO.builder()
                                .id(savedPost.getPost().getCategory().getId())
                                .name(savedPost.getPost().getCategory().getName())
                                .build())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(savedPosts);
    }

    @PostMapping("/posts/{postId}/save")
    public ResponseEntity<?> savePost(@PathVariable Long postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Bezpieczne pobieranie emaila (działa dla UserDto i UserDetails)
            String email;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDto) {
                email = ((UserDto) principal).getEmail();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated correctly");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

            if (savedPostRepository.existsByUserIdAndPostId(user.getId(), postId)) {
                return ResponseEntity.badRequest().body("Post is already saved.");
            }

            // Użycie settera zamiast buildera (dla pewności, że zadziała bez konfiguracji Lomboka)
            SavedPost savedPost = new SavedPost();
            savedPost.setUser(user);
            savedPost.setPost(post);
            // savedPost.setCreatedAt(LocalDateTime.now()); // Odkomentuj jeśli nie masz @PrePersist

            savedPostRepository.save(savedPost);

            return ResponseEntity.ok("Post saved successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // Zobacz błąd w konsoli Java
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving post: " + e.getMessage());
        }
    }

    // --- 2. USUWANIE Z ZAPISANYCH (POPRAWIONE) ---
    @DeleteMapping("/posts/{postId}/unsave")
    @Transactional
    public ResponseEntity<?> unsavePost(@PathVariable Long postId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String email;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDto) {
                email = ((UserDto) principal).getEmail();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated correctly");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (savedPostRepository.existsByUserIdAndPostId(user.getId(), postId)) {
                savedPostRepository.deleteByUserIdAndPostId(user.getId(), postId);
                return ResponseEntity.ok("Post removed from saved.");
            } else {
                return ResponseEntity.badRequest().body("Post was not saved.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error unsaving post: " + e.getMessage());
        }
    }

    // --- 3. SPRAWDZANIE STATUSU (CZY ZAPISANY?) ---
    // Frontend potrzebuje tego, żeby wiedzieć czy wyświetlić pustą czy pełną ikonkę zakładki
    @GetMapping("posts/{postId}/isSaved")
    public ResponseEntity<Boolean> isPostSaved(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Jeśli użytkownik niezalogowany -> false
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(false);
        }

        UserDto principal = (UserDto) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isSaved = savedPostRepository.existsByUserIdAndPostId(user.getId(), postId);
        return ResponseEntity.ok(isSaved);
    }
}
