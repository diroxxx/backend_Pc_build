package org.example.backend_pcbuild.Community.Repository;

import org.example.backend_pcbuild.Community.Models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
