package org.example.backend_pcbuild.Community.Service;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.*;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Repository.CategoryRepository;
import org.example.backend_pcbuild.Community.Repository.CommentRepository;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.example.backend_pcbuild.models.User;
import org.springframework.stereotype.Service;

import javax.xml.stream.events.Comment;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;

    private final CategoryRepository categoryRepository;

    private final CommentRepository commentRepository;


    public Post addPost(User user, CreatePostDTO dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());

        categoryRepository.findById((long) Math.toIntExact(dto.getCategoryId())).ifPresent(post::setCategory);

        return postRepository.save(post);
    }

    public List<PostCommentDTO> getCommentsForPost(Long postId) {
        return commentRepository.findAll()
                .stream()
                .filter(c -> c.getPost().getId().equals(postId))
                .map(c -> {
                    UserPostDTO userDTO = new UserPostDTO(
                            c.getUser().getId(),
                            c.getUser().getUsername()
                    );

                    CategoryDTO categoryDTO = new CategoryDTO(
                            c.getPost().getCategory().getName()
                    );

                    PostDTO postDTO = new PostDTO(
                            c.getPost().getId(),
                            c.getPost().getTitle(),
                            c.getPost().getContent(),
                            categoryDTO
                    );

                    return new PostCommentDTO(
                            c.getId(),
                            c.getContent(),
                            c.getCreatedAt(),
                            userDTO,
                            postDTO
                    );
                })
                .toList();
    }
}
