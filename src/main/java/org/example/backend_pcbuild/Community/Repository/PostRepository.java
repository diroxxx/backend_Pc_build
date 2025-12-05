package org.example.backend_pcbuild.Community.Repository;

import org.example.backend_pcbuild.Community.Models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findByUserId(Long userId);

    List<Post> findByCategoryId(Long categoryId);

    List<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);


    List<Post> findByCategory_Name(String categoryName);
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.images i WHERE p.id = :id")
    Optional<Post> findByIdWithImages(@Param("id") Long id);
}
