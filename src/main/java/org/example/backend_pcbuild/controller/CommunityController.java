package org.example.backend_pcbuild.controller;


import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.*;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.models.User;
import org.springframework.beans.factory.annotation.Autowired;
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


    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @PostMapping("/posts")
    public Post createPost(@RequestBody Post post) {
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    @GetMapping("/posts/{id}")
    public Optional<Post> getPostById(@PathVariable Integer id) {
        return postRepository.findById(id);
    }

    // ============ KOMENTARZE ============

    @GetMapping("/posts/{postId}/comments")
    public List<PostComment> getCommentsForPost(@PathVariable Integer postId) {
        return commentRepository.findAll()
                .stream()
                .filter(c -> c.getPost().getId().equals(postId))
                .toList();
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
