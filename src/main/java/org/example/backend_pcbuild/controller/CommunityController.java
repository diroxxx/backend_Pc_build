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
import org.example.backend_pcbuild.LoginAndRegister.dto.PostResponseDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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


    @GetMapping("/")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }


@PostMapping("/posts")
public Post createPost(@RequestBody CreatePostDTO dto) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
    }

    UserDto principalUserDto = (UserDto) authentication.getPrincipal();


    User user = userRepository.findByEmail(principalUserDto.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found in database."));

    Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));


    Post post = new Post();
    post.setTitle(dto.getTitle());
    post.setContent(dto.getContent());
    post.setUser(user);
    post.setCategory(category);
    post.setCreatedAt(LocalDateTime.now());

    return postRepository.save(post);
}

    @GetMapping("/posts/{id}")
    public Optional<Post> getPostById(@PathVariable Integer id) {
        return postRepository.findById(id);
    }



    @GetMapping("/posts/{postId}/comments")
    public List<PostCommentDTO> getCommentsForPost(@PathVariable Long postId) {
        return communityService.getCommentsForPost(postId);
    }

//    @PostMapping("/posts/{postId}/comments")
//    public PostComment addComment(@PathVariable Integer postId, @RequestBody PostComment comment) {
//        Post post = postRepository.findById(postId).orElseThrow();
//        User user = userRepository.findById(comment.getUser().getId()).orElseThrow();
//
//        comment.setPost(post);
//        comment.setUser(user);
//        comment.setCreatedAt(LocalDateTime.now());
//
//        return commentRepository.save(comment);
//    }

    @PostMapping("/posts/{postId}/comments")
    public PostComment addComment(@PathVariable Integer postId, @RequestBody PostComment dto) {

        // 1. Uwierzytelnienie i Pobranie Principal (jak w createPost)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            // Zwróć 401, jeśli użytkownik nie jest zalogowany
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        // Zakładamy, że principal to UserDto (zgodnie z logiką createPost)
        UserDto principalUserDto = (UserDto) authentication.getPrincipal();

        // 2. Pobranie Autora z Bazy
        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found in database."));

        // 3. Pobranie Posta, do którego dodawany jest komentarz
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));

        // 4. Utworzenie Obiektu Komentarza
        PostComment comment = new PostComment();

        // Ustawienie treści z DTO
        comment.setContent(dto.getContent());

        // Bezpieczne ustawienie relacji i timestampu
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());

        // 5. Zapis do bazy danych
        return commentRepository.save(comment);
    }

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

}
