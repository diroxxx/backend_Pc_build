package org.project.backend_pcbuild.community.repository;

import jakarta.transaction.Transactional;
import org.project.backend_pcbuild.community.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostImageRepository extends JpaRepository<PostImage,Long> {


    @Modifying
    @Transactional
    @Query("DELETE FROM PostImage i WHERE i.post.id = :postId")
    void deleteAllByPostId(Long postId);

}
