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

        // 2. Walidacja Posta
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

        // 4. Logika Głosowania (implementacja logiki serwisu w kontrolerze)
        try {
            Optional<Reaction> existingVoteOpt = reactionRepository.findByPostIdAndUserId(postId, userId);

            if (existingVoteOpt.isPresent()) {
                // SCENARIUSZ 1: Głos ISTNIEJE
                Reaction existingVote = existingVoteOpt.get();

                if (existingVote.getLikeReaction() == isLike) {
                    // SCENARIUSZ 1A: Użytkownik klika TEN SAM GŁOS
                    // Np. Miał Like (true), klika Like (true).
                    // Akcja: Wycofanie głosu (UN-VOTE). Usuwamy rekord, przechodzimy w stan Neutralny.
                    reactionRepository.delete(existingVote);
                } else {
                    // SCENARIUSZ 1B: Użytkownik klika PRZECIWNY GŁOS
                    // Np. Miał Like (true), klika Dislike (false).
                    // Akcja: Zmiana głosu. Aktualizujemy pole 'likeReaction' w istniejącym rekordzie.
                    existingVote.setLikeReaction(isLike);
                    reactionRepository.save(existingVote);
                }
            } else {
                // SCENARIUSZ 2: Głos NIE ISTNIEJE
                // Użytkownik głosuje po raz pierwszy.
                // Akcja: Utworzenie nowego rekordu Reaction.
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

    // Metoda pomocnicza do obliczania wyniku netto (Likes - Dislikes)
    private int getNetScore(Long postId) {
        long likes = reactionRepository.countByPostIdAndLikeReaction(postId, true);
        long dislikes = reactionRepository.countByPostIdAndLikeReaction(postId, false);

        // ZMIANA LOGIKI (jeśli chcesz, aby ujemne wyniki były zerowane):
        // return Math.max(0, netScore); // To by zerowało wszystkie ujemne, co jest niepoprawne dla Twojego systemu.

        // W Twoim przypadku:
        // **Wynik -1 jest POPRAWNY.** Jeśli oczekujesz zera, być może mylisz wynik netto z samym licznikiem polubień.

        return (int) (likes - dislikes); // Pozostaw tak, jak jest.
    }

    @GetMapping("/posts/{postId}/vote/status")
    public ResponseEntity<String> getUserVoteStatus(@PathVariable Long postId) {
        // 1. Uwierzytelnienie i Pobranie Użytkownika
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            // Zwraca null dla niezalogowanych lub 401, w zależności od preferencji.
            // Tutaj zwrócimy null, jeśli użytkownik nie jest zalogowany (brak głosu)
            return ResponseEntity.ok(null);
        }

        UserDto principalUserDto = (UserDto) authentication.getPrincipal();
        User user = userRepository.findByEmail(principalUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        Long userId = user.getId();

        // 2. Znalezienie głosu w bazie
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
            // Brak rekordu w bazie = brak głosu
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

    //    @GetMapping("/image/{imageId}") // Pełna ścieżka to np. /community/image/11
//    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
//
//        try {
//            // 1. Serwis pobiera encję PostImage (z danymi binarnymi)
//            PostImage image = postImageService.getImageById(imageId);
//
//            // 2. Walidacja.
//            if (image.getImage() == null || image.getImage().length == 0) {
//                // Jeśli dane binarne są null lub puste (choć EntytyNotFoundException powinno być pierwsze)
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//            // 3. Zwrócenie danych binarnych z POPRAWNYM nagłówkiem Content-Type
//            return ResponseEntity.ok()
//                    // ❗ KLUCZOWE: Ustawienie typu MIME (np. image/png, image/jpeg)
//                    .contentType(MediaType.parseMediaType(image.getMimeType()))
//                    // Wysłanie surowej tablicy bajtów (dane binarne obrazu)
//                    .body(image.getImage());
//
//        } catch (EntityNotFoundException e) {
//            // Obsługa, jeśli PostImage nie istnieje w bazie (zwróci 404 NOT FOUND)
//            // Używamy ResponseEntity.status().build() dla czystszej odpowiedzi 404
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (Exception e) {
//            // Ogólna obsługa błędu
//            System.err.println("Błąd serwowania obrazu: " + e.getMessage());
//            // Zwracamy 500 INTERNAL SERVER ERROR
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {

        try {
            // Pobierz encję obrazu z bazy
            PostImage image = postImageService.getImageById(imageId);

            if (image.getImage() == null || image.getImage().length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Ustal MIME typu np. image/jpeg, image/png
            String mimeType = image.getMimeType();
            MediaType mediaType = MediaType.parseMediaType(mimeType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(image.getImage().length)        // ⭐ Kluczowe dla przeglądarek!
                    .cacheControl(CacheControl.noCache())         // opcjonalnie
                    .body(image.getImage());                      // surowe bytes[]

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            System.err.println("Błąd serwowania obrazu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //    @GetMapping(value = "/posts/{postId}/images", produces = MediaType.TEXT_HTML_VALUE)
//    public ResponseEntity<String> renderImagesForPost(@PathVariable Long postId) {
//
//        try {
//            Post post = postImageService.getPostDetails(postId);
//
//            StringBuilder html = new StringBuilder();
//            html.append("<html><body style='display:flex; flex-wrap:wrap; gap:20px;'>");
//
//            for (PostImage img : post.getImages()) {
//                html.append("<div>");
//                html.append("<img src=\"/community/image/" + img.getId() + "\" ")
//                        .append("alt=\"" + img.getFilename() + "\" ")
//                        .append("style=\"max-width:300px; height:auto; border:1px solid #ccc;\"/>");
//                html.append("</div>");
//            }
//
//            html.append("</body></html>");
//
//            return ResponseEntity.ok(html.toString());
//
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("<h1>Post o ID " + postId + " nie istnieje</h1>");
//        }
    @GetMapping(value = "/posts/{postId}/images", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PostImageDTO>> getImagesForPost(@PathVariable Long postId) {

        try {
            // 1. Pobieranie szczegółów posta z JAWNIE ZAŁADOWANYMI obrazami (JOIN FETCH)
            Post post = postImageService.getPostDetails(postId); // Używamy metody z JOIN FETCH

            // 2. Mapowanie Set<PostImage> (z encji) na List<PostImageDTO> (na zewnątrz)
            List<PostImageDTO> imageDTOs = post.getImages().stream()
                    .map(img -> {
                        // UWAGA: PostImageDTO musi mieć konstruktor/builder,
                        // który generuje pole 'imageUrl' na podstawie id i mimeType.
                        return new PostImageDTO(
                                img.getId(),
                                img.getFilename(),
                                img.getMimeType()
                        );
                    })
                    .collect(Collectors.toList()); // Konwersja na List

            // 3. Zwracanie listy DTO (zostanie automatycznie skonwertowana na JSON)
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
}
