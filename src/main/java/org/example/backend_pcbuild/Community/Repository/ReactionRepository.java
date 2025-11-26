package org.example.backend_pcbuild.Community.Repository;

import org.example.backend_pcbuild.Community.Models.Post;
import org.example.backend_pcbuild.Community.Models.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction,Integer> {
    Optional<Reaction> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostIdAndLikeReaction(Long postId, boolean likeReaction);
}
