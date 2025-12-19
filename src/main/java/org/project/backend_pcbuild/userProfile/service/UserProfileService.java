package org.project.backend_pcbuild.userProfile.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.project.backend_pcbuild.community.dto.CategoryDTO;
import org.project.backend_pcbuild.community.model.Post;
import org.project.backend_pcbuild.community.model.SavedPost;
import org.project.backend_pcbuild.community.repository.PostRepository;
import org.project.backend_pcbuild.loginAndRegister.repository.UserRepository;
import org.project.backend_pcbuild.userProfile.dto.SavedPostDTO;
import org.project.backend_pcbuild.userProfile.dto.UserPostsDTO;
import org.project.backend_pcbuild.userProfile.repository.SavedPostRepository;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserProfileService {

    private final PostRepository postRepository;
    private final SavedPostRepository savedPostRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserPostsDTO> getUserPosts(User user) {
        List<Post> posts = postRepository.findByUserId(user.getId());
        return mapToUserPostsDTO(posts, user.getId());
    }

    @Transactional(readOnly = true)
    public List<UserPostsDTO> getPostsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        List<Post> posts = postRepository.findByUserId(user.getId());
        return mapToUserPostsDTO(posts, user.getId());
    }

    private List<UserPostsDTO> mapToUserPostsDTO(List<Post> posts, Long userId) {
        return posts.stream().map(post -> {
            Long imgId = (post.getImages() != null && !post.getImages().isEmpty())
                    ? post.getImages().iterator().next().getId()
                    : null;

            return UserPostsDTO.builder()
                    .id(post.getId())
                    .userId(userId)
                    .title(post.getTitle())
                    .content(post.getContent())
                    .category(new CategoryDTO(post.getCategory().getId(), post.getCategory().getName()))
                    .imageId(imgId)
                    .createdAt(post.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SavedPostDTO> getSavedPostsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        List<SavedPost> savedEntities = savedPostRepository.findByUserId(user.getId());

        return savedEntities.stream()
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
    }

    @Transactional
    public void savePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (savedPostRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            throw new IllegalStateException("Post is already saved.");
        }

        SavedPost savedPost = new SavedPost();
        savedPost.setUser(user);
        savedPost.setPost(post);
        savedPostRepository.save(savedPost);
    }

    @Transactional
    public void unsavePost(Long postId, User user) {
        if (!savedPostRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            throw new EntityNotFoundException("Post was not saved.");
        }
        savedPostRepository.deleteByUserIdAndPostId(user.getId(), postId);
    }

    public boolean isPostSaved(Long postId, User user) {
        return savedPostRepository.existsByUserIdAndPostId(user.getId(), postId);
    }
}