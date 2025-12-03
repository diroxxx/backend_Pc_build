package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.GameGpuRequirements;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameGpuRequirementsRepository extends JpaRepository<GameGpuRequirements, Long> {
    public void deleteByGameId(Long gameId);
}
