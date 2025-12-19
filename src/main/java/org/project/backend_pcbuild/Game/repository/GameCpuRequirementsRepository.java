package org.project.backend_pcbuild.Game.repository;

import org.project.backend_pcbuild.Game.model.GameCpuRequirements;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameCpuRequirementsRepository extends JpaRepository<GameCpuRequirements, Long> {


    public  void deleteByGameId(Long gameId);
}
