package org.example.backend_pcbuild.Community.Repository;

import org.example.backend_pcbuild.Community.Models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<PostComment,Integer> {
}
