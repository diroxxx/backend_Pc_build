package org.example.backend_pcbuild.Community.Repository;

import org.example.backend_pcbuild.Community.Models.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedPostRepository extends JpaRepository<SavedPost,Long> {
}
