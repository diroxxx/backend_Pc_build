package org.project.backend_pcbuild.Game.repository;

import org.project.backend_pcbuild.Game.model.GameGpuRequirements;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameGpuRequirementsRepository extends JpaRepository<GameGpuRequirements, Long> {
    void deleteByGameId(Long gameId);
}
