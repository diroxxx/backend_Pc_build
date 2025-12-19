package org.project.backend_pcbuild.community.repository;

import org.project.backend_pcbuild.community.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
