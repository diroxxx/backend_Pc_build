package org.project.backend_pcbuild.community.repository;

import org.project.backend_pcbuild.community.model.ReactionComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionCommentRepository extends JpaRepository<ReactionComment,Long> {

    Optional<ReactionComment> findByCommentIdAndUserId(Long commentId, Long userId);

    long countByCommentIdAndLikeReaction(Long commentId, Boolean likeReaction);

    void deleteAllByCommentId(Long commentId);
}
