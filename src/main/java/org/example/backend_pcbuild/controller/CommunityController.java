package org.example.backend_pcbuild.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.*;
import org.example.backend_pcbuild.Community.Models.*;
import org.example.backend_pcbuild.Community.Repository.*;
import org.example.backend_pcbuild.Community.Service.CommunityService;
import org.example.backend_pcbuild.Community.Service.PostImageService;
import org.example.backend_pcbuild.Community.Service.SavedPostService;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.UserProfile.SavedPostRepository;
import org.example.backend_pcbuild.models.User;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/community")
public class CommunityController {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PostCommentRepository commentRepository;
    private final CommunityService communityService;
    private final ReactionRepository reactionRepository;
    private final PostImageRepository postImageRepository;
    private final PostImageService postImageService;
    private final SavedPostService savedPostService;
    private final SavedPostRepository savedPostRepository;
    private final ReactionCommentRepository reactionCommentRepository;

    @GetMapping("/")
    public List<PostPreviewDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> {
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
                            firstImageId
                    );
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/posts")
    public Post createPost(@RequestBody CreatePostDTO dto) {

        User user = getAuthenticatedUser();

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
    public Optional<Post> getPostById(@PathVariable Long id) {
        return postRepository.findById(id);
    }

    @GetMapping("/posts/{postId}/comments")
    public List<PostCommentDTO> getCommentsForPost(@PathVariable Long postId) {
        return communityService.getCommentsForPost(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public PostComment addComment(@PathVariable Long postId, @RequestBody PostComment dto) {

        User user = getAuthenticatedUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));

        PostComment comment = new PostComment();
        comment.setContent(dto.getContent());
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    @GetMapping("/categories/id/{categoryId}")
    public ResponseEntity<List<PostPreviewDTO>> getPostsByCategoryId(@PathVariable Long categoryId) {
        List<Post> posts = postRepository.findByCategoryId(categoryId);

        if (posts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<PostPreviewDTO> postDTOs = posts.stream()
                .map(post -> {
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
                            firstImageId
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(postDTOs);
    }

    @PostMapping(value = "/posts/upload-image-to-db", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImageToDatabase(
            @RequestPart("file") MultipartFile file,
            @RequestParam("postId") String postId) {

        if (file.isEmpty() || file.getOriginalFilename() == null) {
            return ResponseEntity.badRequest().body("Plik nie może być pusty.");
        }

        if (!file.getContentType().startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Obsługiwane są tylko pliki graficzne.");
        }

        try {
            byte[] imageData = file.getBytes();
            String mimeType = file.getContentType();

            PostImageDTO savedImage = postImageService.addImageToPost(Long.parseLong(postId), imageData, mimeType, file.getOriginalFilename());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd odczytu pliku: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nieznany błąd podczas zapisu zdjęcia.");
        }
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        try {
            PostImage image = postImageService.getImageById(imageId);

            if (image.getImage() == null || image.getImage().length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String mimeType = image.getMimeType();
            MediaType mediaType = MediaType.parseMediaType(mimeType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(image.getImage().length)
                    .cacheControl(CacheControl.noCache())
                    .body(image.getImage());

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Błąd serwowania obrazu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/posts/{postId}/images", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PostImageDTO>> getImagesForPost(@PathVariable Long postId) {
        try {
            Post post = postImageService.getPostDetails(postId);

            List<PostImageDTO> imageDTOs = post.getImages().stream()
                    .map(img -> new PostImageDTO(
                            img.getId(),
                            img.getFilename(),
                            img.getMimeType()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(imageDTOs);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post nie znaleziony o ID: " + postId);
        } catch (Exception e) {
            System.err.println("Błąd pobierania listy obrazów dla posta: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Wystąpił błąd serwera podczas pobierania zdjęć.");
        }
    }

    @PutMapping("/posts/{postId}")
    @Transactional
    public ResponseEntity<Post> updatePost(@PathVariable Long postId, @RequestBody UpdatePostDTO dto) {

        User currentUser = getAuthenticatedUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));

        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = String.valueOf(currentUser.getRole()).equals("ADMIN");

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content cannot be empty.");
        }

        post.setContent(dto.getContent());

        return ResponseEntity.ok(postRepository.save(post));
    }


    @DeleteMapping("/posts/delete/{postId}")
    @Transactional
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        User currentUser = getAuthenticatedUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));

        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = String.valueOf(currentUser.getRole()).equals("ADMIN");

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {

            if (post.getComments() != null && !post.getComments().isEmpty()) {
                List<PostComment> commentsToDelete = new ArrayList<>(post.getComments());


                post.setComments(new HashSet<>());

                for (PostComment comment : commentsToDelete) {
                    if (comment.getReactions() != null && !comment.getReactions().isEmpty()) {
                        reactionCommentRepository.deleteAll(comment.getReactions());
                    }
                }
                commentRepository.deleteAll(commentsToDelete);
            }

            reactionRepository.deleteAllByPostId(postId);
            postImageRepository.deleteAllByPostId(postId);
            postRepository.delete(post);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting post: " + e.getMessage());
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        try {
            UserDto principalUserDto = (UserDto) authentication.getPrincipal();
            return userRepository.findByEmail(principalUserDto.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found in database."));
        } catch (ClassCastException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal.");
        }
    }
}