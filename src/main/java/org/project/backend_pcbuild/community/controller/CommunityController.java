package org.project.backend_pcbuild.community.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.project.backend_pcbuild.community.dto.*;
import org.project.backend_pcbuild.community.model.Category;
import org.project.backend_pcbuild.community.model.Post;
import org.project.backend_pcbuild.community.model.PostComment;
import org.project.backend_pcbuild.community.model.PostImage;
import org.project.backend_pcbuild.community.repository.CategoryRepository;
import org.project.backend_pcbuild.community.repository.PostRepository;
import org.project.backend_pcbuild.community.service.CommentService;
import org.project.backend_pcbuild.community.service.PostImageService;
import org.project.backend_pcbuild.community.service.PostService;
import org.project.backend_pcbuild.loginAndRegister.dto.UserDto;
import org.project.backend_pcbuild.loginAndRegister.repository.UserRepository;
import org.project.backend_pcbuild.usersManagement.model.User;
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
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/community")
public class CommunityController {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PostService communityService;
    private final PostImageService postImageService;
    private final CommentService commentService;
    private final PostService postService;


    //posty
    @GetMapping("/")
    public ResponseEntity<List<PostPreviewDTO>> getAllPosts() {
        List<PostPreviewDTO> posts = postService.getAllPosts();

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{id}")
    public Optional<Post> getPostById(@PathVariable Long id) {
        return postRepository.findById(id);
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody CreatePostDTO dto) {
        User user = getAuthenticatedUser();

        try {
            Post createdPost = postService.createPost(dto, user);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating post");
        }
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable Long postId, @RequestBody UpdatePostDTO dto) {
        User user = getAuthenticatedUser();
        try {
            Post updatedPost = postService.updatePost(postId, dto, user);
            return ResponseEntity.ok(updatedPost);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/posts/delete/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        User user = getAuthenticatedUser();
        try {
            postService.deletePost(postId, user);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting post");
        }
    }

    @GetMapping("/categories/id/{categoryId}")
    public ResponseEntity<List<PostPreviewDTO>> getPostsByCategoryId(@PathVariable Long categoryId) {

        List<PostPreviewDTO> postDTOs = postService.getPostsByCategoryId(categoryId);

        if (postDTOs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(postDTOs);
    }


    @GetMapping(value = "/posts/{postId}/images", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PostImageDTO>> getImagesForPost(@PathVariable Long postId) {
        try {
            List<PostImageDTO> images = postService.getImagesForPost(postId);

            return ResponseEntity.ok(images);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Wystąpił błąd serwera podczas pobierania zdjęć.");
        }
    }

    //komentarze
    @GetMapping("/posts/{postId}/comments")
    public List<PostCommentDTO> getCommentsForPost(@PathVariable Long postId) {
        return communityService.getCommentsForPost(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<PostComment> addComment(@PathVariable Long postId, @RequestBody PostComment dto) {
        User user = getAuthenticatedUser();
        PostComment createdComment = commentService.addComment(postId, dto.getContent(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<PostComment> updateComment(@PathVariable Long commentId, @RequestBody PostComment dto) {
        User user = getAuthenticatedUser();
        try {
            PostComment updated = commentService.updateComment(commentId, dto.getContent(), user);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        User user = getAuthenticatedUser();
        try {
            commentService.deleteComment(commentId, user);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting comment");
        }
    }


    //kategorie
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    //zdjecia
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

            PostImageDTO savedImage = postImageService.addImageToPost(
                    Long.parseLong(postId),
                    imageData,
                    mimeType,
                    file.getOriginalFilename()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd odczytu pliku: " + e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nieznany błąd podczas zapisu zdjęcia.");
        }
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        try {
            PostImage image = postImageService.getImageById(imageId);

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