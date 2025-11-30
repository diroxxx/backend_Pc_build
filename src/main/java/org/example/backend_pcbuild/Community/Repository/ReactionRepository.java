package org.example.backend_pcbuild.Community.Repository;

import jakarta.transaction.Transactional;
import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction,Integer> {
    Optional<Reaction> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostIdAndLikeReaction(Long postId, boolean likeReaction);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reaction r WHERE r.post.id = :postId")
    void deleteAllByPostId(Long postId);
}
