package org.example.backend_pcbuild.controller;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.*;
import org.example.backend_pcbuild.Community.Models.*;
import org.example.backend_pcbuild.Community.Repository.*;
import org.example.backend_pcbuild.Community.Service.CommunityService;
import org.example.backend_pcbuild.Community.Service.PostImageService;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.PostResponseDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private PostImageService postImageService;


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
    public Optional<Post> getPostById(@PathVariable Long id) {
        return postRepository.findById(id);
    }


    @GetMapping("/posts/{postId}/comments")
    public List<PostCommentDTO> getCommentsForPost(@PathVariable Long postId) {
        return communityService.getCommentsForPost(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public PostComment addComment(@PathVariable Long postId, @RequestBody PostComment dto) {


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();

        // 2. Pobranie Autora z Bazy
        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found in database."));


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


    @PostMapping("/posts/{postId}/vote")
    @Transactional // Wymagana transakcja do modyfikacji danych
    public ResponseEntity<Integer> castVote(
            @PathVariable Long postId,
            @RequestParam String type
    ) {
        // 1. Uwierzytelnienie i Pobranie Użytkownika
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        Long userId = user.getId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));


        final boolean isLike;
        if ("upvote".equalsIgnoreCase(type)) {
            isLike = true;
        } else if ("downvote".equalsIgnoreCase(type)) {
            isLike = false;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vote type. Must be 'upvote' or 'downvote'.");
        }

        try {
            Optional<Reaction> existingVoteOpt = reactionRepository.findByPostIdAndUserId(postId, userId);

            if (existingVoteOpt.isPresent()) {
                // SCENARIUSZ 1: Głos ISTNIEJE
                Reaction existingVote = existingVoteOpt.get();

                if (existingVote.getLikeReaction() == isLike) {
                    reactionRepository.delete(existingVote);
                } else {
                    existingVote.setLikeReaction(isLike);
                    reactionRepository.save(existingVote);
                }
            } else {
                Reaction newVote = new Reaction();
                newVote.setPost(post);
                newVote.setUser(user);
                newVote.setLikeReaction(isLike);
                reactionRepository.save(newVote);
            }

            // 5. Obliczenie i zwrot nowej punktacji netto
            return ResponseEntity.ok(getNetScore(postId));

        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error during vote: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{postId}/vote")
    public ResponseEntity<Integer> getScore(@PathVariable Long postId) {
        try {
            int score = getNetScore(postId);
            return ResponseEntity.ok(score);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving score.");
        }
    }

    private int getNetScore(Long postId) {
        long likes = reactionRepository.countByPostIdAndLikeReaction(postId, true);
        long dislikes = reactionRepository.countByPostIdAndLikeReaction(postId, false);

        return (int) (likes - dislikes); // Pozostaw tak, jak jest.
    }

    @GetMapping("/posts/{postId}/vote/status")
    public ResponseEntity<String> getUserVoteStatus(@PathVariable Long postId) {
        // 1. Uwierzytelnienie i Pobranie Użytkownika
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(null);
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        Long userId = user.getId();

        Optional<Reaction> existingVoteOpt = reactionRepository.findByPostIdAndUserId(postId, userId);

        if (existingVoteOpt.isPresent()) {
            Reaction vote = existingVoteOpt.get();
            // 3. Mapowanie typu głosu z Boolean na String
            if (vote.getLikeReaction()) {
                return ResponseEntity.ok("upvote");
            } else {
                return ResponseEntity.ok("downvote");
            }
        } else {
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping(value = "/posts/upload-image-to-db", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImageToDatabase(
            @RequestPart("file") MultipartFile file,
            @RequestParam("postId") String postId) {


        if (file.isEmpty() || file.getOriginalFilename() == null) {
            return ResponseEntity.badRequest().body("Plik nie może być pusty.");
        }

        // Sprawdzenie typu MIME dla bezpieczeństwa
        if (!file.getContentType().startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Obsługiwane są tylko pliki graficzne.");
        }

        try {
            // 2. Przekazanie danych binarnych do Serwisu
            byte[] imageData = file.getBytes();
            String mimeType = file.getContentType();

            PostImageDTO savedImage = postImageService.addImageToPost(Long.parseLong(postId), imageData, mimeType, file.getOriginalFilename());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
        } catch (IOException e) {
            // Błąd odczytu pliku
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd odczytu pliku: " + e.getMessage());
        } catch (IllegalStateException e) {
            // Błąd z Serwisu (np. przekroczenie limitu 5 zdjęć)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Ogólny błąd
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
                    .map(img -> {
                        return new PostImageDTO(
                                img.getId(),
                                img.getFilename(),
                                img.getMimeType()
                        );
                    })
                    .collect(Collectors.toList()); // Konwersja na List

            return ResponseEntity.ok(imageDTOs);

        } catch (EntityNotFoundException e) {
            // Zwracanie 404 NOT FOUND, jeśli post nie istnieje
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post nie znaleziony o ID: " + postId);

        } catch (Exception e) {
            // Obsługa ogólnych błędów serwera
            System.err.println("Błąd pobierania listy obrazów dla posta: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Wystąpił błąd serwera podczas pobierania zdjęć.");
        }
    }

    @PutMapping("/posts/{postId}")
    @Transactional
    public ResponseEntity<Post> updatePost(@PathVariable Long postId, @RequestBody UpdatePostDTO dto) {

        // 1. Uwierzytelnienie (Bez zmian)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        User currentUser = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        // 2. Pobranie Posta (Bez zmian)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));

        // 3. Autoryzacja: Tylko autor lub admin (Bez zmian)
        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = principalUserDto.getRole().equals("ADMIN");

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 4. ⭐ WALIDACJA I AKTUALIZACJA TYLKO TREŚCI ⭐
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content cannot be empty.");
        }

        // Aktualizujemy tylko treść
        post.setContent(dto.getContent());

        return ResponseEntity.ok(postRepository.save(post));
    }

    @DeleteMapping("/posts/delete/{postId}")
    @Transactional
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        User currentUser = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));

        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = principalUserDto.getRole().equals("ADMIN");

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            commentRepository.deleteAllByPostId(postId);

            reactionRepository.deleteAllByPostId(postId);
            postImageRepository.deleteAllByPostId(postId);

            // 5. Usunięcie głównej encji
            postRepository.delete(post);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException e) {
            System.err.println("Błąd bazy danych podczas usuwania posta " + postId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error during post deletion. Check related entities.");
        }
    }
}
