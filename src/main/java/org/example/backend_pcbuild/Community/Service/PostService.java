package org.example.backend_pcbuild.Community.Service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.*;
import org.example.backend_pcbuild.Community.Models.Category;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.PostComment;
import org.example.backend_pcbuild.Community.Repository.*;
import org.example.backend_pcbuild.models.User;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostCommentRepository postCommentRepository;
    private final ReactionRepository reactionRepository;
    private final PostImageRepository postImageRepository;
    private final ReactionCommentRepository reactionCommentRepository;


    private PostPreviewDTO mapToPreviewDTO(Post post) {
        Long firstImageId = null;
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            firstImageId = post.getImages().iterator().next().getId();
        }

        return new PostPreviewDTO(
                post.getId(),
                post.getTitle(),
                post.getContent().length() > 100 ? post.getContent().substring(0, 100) + "..." : post.getContent(),
                post.getUser().getUsername(),
                post.getCreatedAt(),
                post.getCategory() != null ? post.getCategory().getName() : "Ogólne",
                firstImageId
        );
    }

    @Transactional
    public List<PostPreviewDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapToPreviewDTO)
                .collect(Collectors.toList());
    }


    public List<PostCommentDTO> getCommentsForPost(Long postId) {
        return postCommentRepository.findAll()
                .stream()
                .filter(c -> c.getPost().getId().equals(postId))
                .map(c -> {
                    UserPostDTO userDTO = new UserPostDTO(
                            c.getUser().getId(),
                            c.getUser().getUsername()
                    );

                    CategoryDTO categoryDTO = new CategoryDTO(
                            c.getPost().getCategory().getId(),
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
                            postDTO,
                            userDTO.getUsername()
                    );
                })
                .toList();
    }
    @Transactional
    public Post createPost(CreatePostDTO dto, User user) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(user);
        post.setCategory(category);
        post.setCreatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }
    @Transactional
    public Post updatePost(Long postId, UpdatePostDTO dto, User currentUser) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        validatePostOwnership(post, currentUser);

        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty.");
        }

        post.setContent(dto.getContent());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, User currentUser) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        validatePostOwnership(post, currentUser);


        if (post.getComments() != null && !post.getComments().isEmpty()) {
            List<PostComment> commentsToDelete = new ArrayList<>(post.getComments());

            post.setComments(new HashSet<>());

            for (PostComment comment : commentsToDelete) {

                if (comment.getReactions() != null && !comment.getReactions().isEmpty()) {
                    reactionCommentRepository.deleteAll(comment.getReactions());
                }
            }
            postCommentRepository.deleteAll(commentsToDelete);
        }

        reactionRepository.deleteAllByPostId(postId);

        postImageRepository.deleteAllByPostId(postId);

        postRepository.delete(post);
    }
    private void validatePostOwnership(Post post, User currentUser) throws AccessDeniedException {
        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = String.valueOf(currentUser.getRole()).equals("ADMIN");

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to modify/delete this post.");
        }
    }
    @Transactional
    public List<PostImageDTO> getImagesForPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        return post.getImages().stream()
                .map(img -> new PostImageDTO(
                        img.getId(),
                        img.getFilename(),
                        img.getMimeType()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PostPreviewDTO> getPostsByCategoryId(Long categoryId) {
        return postRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToPreviewDTO)
                .collect(Collectors.toList());
    }

}
