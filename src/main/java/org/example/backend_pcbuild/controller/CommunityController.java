package org.example.backend_pcbuild.controller;


import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.*;
import org.example.backend_pcbuild.Community.Models.Category;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.PostComment;
import org.example.backend_pcbuild.Community.Repository.CategoryRepository;
import org.example.backend_pcbuild.Community.Repository.PostCommentRepository;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.Community.Service.CommunityService;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
//@CrossOrigin("http://127.0.0.1:5000")
@AllArgsConstructor
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostCommentRepository commentRepository;

    @Autowired
    private CommunityService communityService;


    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }


    @PostMapping("/posts")

//    public Post createPost(@RequestBody CreatePostDTO dto) {
//        User user = userRepository.findById(dto.getUserId())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new RuntimeException("Category not found"));
//
//        Post post = new Post();
//        post.setTitle(dto.getTitle());
//        post.setContent(dto.getContent());
//        post.setUser(user);
//        post.setCategory(category);
//        post.setCreatedAt(LocalDateTime.now());
//
//        return postRepository.save(post);
//    }
    public Post createPost(@RequestBody CreatePostDTO dto, @AuthenticationPrincipal User currentUser) {

        // ⚠️ Krok 1: Weryfikacja zalogowanego użytkownika
        if (currentUser == null) {
            // Zabezpieczenie na wypadek, gdyby Security nie zadziałało poprawnie,
            // ale standardowo powinno to być zablokowane filtrem JWT/Security.
            throw new RuntimeException("Unauthorized: User not logged in.");
        }

        // ⚠️ Krok 2: Używamy obiektu Usera z Security zamiast z DTO
        User user = currentUser;

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(user); // Ustawiamy Usera pobranego z Security
        post.setCategory(category);
        post.setCreatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }
    @GetMapping("/posts/{id}")
    public Optional<Post> getPostById(@PathVariable Integer id) {
        return postRepository.findById(id);
    }


    // ============ KOMENTARZE ============

    @GetMapping("/posts/{postId}/comments")
    public List<PostCommentDTO> getCommentsForPost(@PathVariable Long postId) {
        return communityService.getCommentsForPost(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public PostComment addComment(@PathVariable Integer postId, @RequestBody PostComment comment) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findById(comment.getUser().getId()).orElseThrow();

        comment.setPost(post);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }


    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

}
