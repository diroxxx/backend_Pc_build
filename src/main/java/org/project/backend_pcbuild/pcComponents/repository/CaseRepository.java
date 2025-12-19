package org.project.backend_pcbuild.pcComponents.repository;

import org.project.backend_pcbuild.pcComponents.model.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<Case, Integer> {
}
