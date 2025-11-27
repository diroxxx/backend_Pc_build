package org.example.backend_pcbuild.Community.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.PostImageDTO;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.PostImage;
import org.example.backend_pcbuild.Community.Repository.PostImageRepository;
import org.example.backend_pcbuild.Community.Repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//@Service
//@Transactional(readOnly = true)
//@AllArgsConstructor
//public class PostImageService {
//
//    private static final int MAX_IMAGES_PER_POST = 5;
//    private final PostRepository postRepository;
//    private final PostImageRepository postImageRepository;
//
//    @Transactional
//    public PostImageDTO addImageToPost(
//            Long postId,
//            byte[] imageData,
//            String mimeType,
//            String originalFilename) {
//
//        // 1. Walidacja: Znajdź Post
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new EntityNotFoundException("Post nie znaleziony o ID: " + postId));
//
//        // 2. Walidacja: Sprawdzenie limitu (max 5 zdjęć)
//        if (post.getImages().size() >= MAX_IMAGES_PER_POST) {
//            throw new IllegalStateException("Osiągnięto maksymalny limit " + MAX_IMAGES_PER_POST + " zdjęć dla tego postu.");
//        }
//
//        // 3. Tworzenie i mapowanie encji PostImage
//        PostImage newImage = new PostImage();
//        newImage.setImage(imageData); // Ustawienie danych binarnych
//        newImage.setMimeType(mimeType);
//        newImage.setFilename(originalFilename);
//        newImage.setPost(post); // Ustawienie relacji ManyToOne
//
//        // 4. Zapis do bazy
//        PostImage savedImage = postImageRepository.save(newImage);
//
//        // 5. Aktualizacja kolekcji w obiekcie Post (opcjonalne, ale dobra praktyka)
//        post.getImages().add(savedImage);
//
//        // 6. Konwersja i zwrot DTO
//        return new PostImageDTO(savedImage.getId(), savedImage.getFilename(), savedImage.getMimeType());
//    }
//}


// package org.example.backend_pcbuild.Community.Service;
// ... (inne importy)

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class PostImageService {

    private static final int MAX_IMAGES_PER_POST = 5;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    @Transactional
    public PostImageDTO addImageToPost(
            Long postId,
            byte[] imageData,
            String mimeType,
            String originalFilename) {

        // 1. Walidacja: Znajdź Post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post nie znaleziony o ID: " + postId));

        // 2. Walidacja: Sprawdzenie limitu (limit jest bezpieczny, bo odczyt następuje przed modyfikacją)
        if (post.getImages().size() >= MAX_IMAGES_PER_POST) {
            throw new IllegalStateException("Osiągnięto maksymalny limit " + MAX_IMAGES_PER_POST + " zdjęć dla tego postu.");
        }

        // 3. Tworzenie i mapowanie encji PostImage
        PostImage newImage = new PostImage();
        newImage.setImage(imageData);
        newImage.setMimeType(mimeType);
        newImage.setFilename(originalFilename);
        newImage.setPost(post); // KLUCZOWE: Ustawienie klucza obcego dla bazy danych

        // 4. Zapis do bazy
        PostImage savedImage = postImageRepository.save(newImage);

        // 5. USUNIĘTO: Aktualizacja kolekcji w obiekcie Post
        // post.getImages().add(savedImage); <--- TA LINIA ZOSTAŁA USUNIĘTA!

        // 6. Konwersja i zwrot DTO
        return new PostImageDTO(savedImage.getId(), savedImage.getFilename(), savedImage.getMimeType());
    }

    public PostImage getImageById(Long imageId) {
        return postImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Obraz nie znaleziony o ID: " + imageId));
    }

    public Post getPostDetails(Long postId) {

        // Wywołujemy metodę Repozytorium, która ma @Query z JOIN FETCH (findByIdWithImages).
        // To jest moment, w którym Hibernate pobiera Post i jego Images naraz.
        return postRepository.findByIdWithImages(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post nie znaleziony o ID: " + postId));
    }
}