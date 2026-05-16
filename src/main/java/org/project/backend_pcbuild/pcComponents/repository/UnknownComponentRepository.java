package org.project.backend_pcbuild.pcComponents.repository;

import org.project.backend_pcbuild.pcComponents.model.UnknownComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnknownComponentRepository extends JpaRepository<UnknownComponent, Long> {
    Optional<UnknownComponent> findFirstByModel(String model);
}
