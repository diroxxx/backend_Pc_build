package org.project.backend_pcbuild.community.repository;

import jakarta.transaction.Transactional;
import org.project.backend_pcbuild.community.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment,Long> {

    @Query("SELECT pc FROM PostComment pc JOIN FETCH pc.user u WHERE pc.post.id = :postId ORDER BY pc.createdAt ASC")
    List<PostComment> findCommentsByPostId(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PostComment c WHERE c.post.id = :postId")
    void deleteAllByPostId(Long postId);
}
