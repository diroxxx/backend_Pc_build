package org.project.backend_pcbuild.community.repository;

import org.project.backend_pcbuild.community.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<PostComment,Integer> {
}
