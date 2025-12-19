package org.project.backend_pcbuild.userProfile.repository;

import jakarta.transaction.Transactional;
import org.project.backend_pcbuild.community.model.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedPostRepository extends JpaRepository<SavedPost,Long> {

    List<SavedPost> findAllByUserId(Long userId);

    // Pobieranie zapisanych przez usera (już to masz)
    List<SavedPost> findByUserId(Long userId);

    // Sprawdzenie czy dany user zapisał dany post
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // Znalezienie konkretnego zapisu (potrzebne do usunięcia)
    Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);

    // Usuwanie zapisu
    @Transactional
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
