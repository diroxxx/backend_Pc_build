package org.project.backend_pcbuild.userProfile.repository;

import jakarta.transaction.Transactional;
import org.project.backend_pcbuild.community.model.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedPostRepository extends JpaRepository<SavedPost,Long> {

    List<SavedPost> findByUserId(Long userId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);
    @Transactional
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
