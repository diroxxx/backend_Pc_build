package org.example.backend_pcbuild.Community.Service;


import jakarta.transaction.Transactional;
import org.example.backend_pcbuild.Community.DTO.CreatePostDTO;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Repository.CategoryRepository;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.models.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class CommunityService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public CommunityService(PostRepository postRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    public Post addPost(User user, CreatePostDTO dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(user);  // przypisanie zalogowanego użytkownika
        post.setCreatedAt(LocalDateTime.now());

        categoryRepository.findById((long) Math.toIntExact(dto.getCategoryId())).ifPresent(post::setCategory);

        return postRepository.save(post);
    }
}
