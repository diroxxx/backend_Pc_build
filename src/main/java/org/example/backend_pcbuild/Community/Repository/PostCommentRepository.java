package org.example.backend_pcbuild.Community.Repository;

import org.example.backend_pcbuild.Community.Models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment,Integer> {

    @Query("SELECT pc FROM PostComment pc JOIN FETCH pc.user u WHERE pc.post.id = :postId ORDER BY pc.createdAt ASC")
    List<PostComment> findCommentsByPostId(@Param("postId") Long postId);
}
