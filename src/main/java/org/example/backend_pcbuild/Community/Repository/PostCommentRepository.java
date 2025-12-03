package org.example.backend_pcbuild.Community.Repository;

import jakarta.transaction.Transactional;
import org.example.backend_pcbuild.Community.Models.PostComment;
import org.example.backend_pcbuild.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment,Long> {

    @Query("SELECT pc FROM PostComment pc JOIN FETCH pc.user u WHERE pc.post.id = :postId ORDER BY pc.createdAt ASC")
    List<PostComment> findCommentsByPostId(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PostComment c WHERE c.post.id = :postId")
    void deleteAllByPostId(Long postId);
}
