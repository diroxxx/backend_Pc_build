package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<Case, Integer> {
}
