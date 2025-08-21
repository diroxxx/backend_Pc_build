package org.example.backend_pcbuild.Community.Repository;

import org.example.backend_pcbuild.Community.Models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Integer> {
}
