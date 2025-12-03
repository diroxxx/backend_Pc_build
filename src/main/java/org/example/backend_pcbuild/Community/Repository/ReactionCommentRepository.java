package org.example.backend_pcbuild.Community.Repository;

import org.example.backend_pcbuild.Community.Models.ReactionComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionCommentRepository extends JpaRepository<ReactionComment,Long> {

    Optional<ReactionComment> findByCommentIdAndUserId(Long commentId, Long userId);

    int countByCommentIdAndLikeReaction(Long commentId, boolean likeReaction);
}
