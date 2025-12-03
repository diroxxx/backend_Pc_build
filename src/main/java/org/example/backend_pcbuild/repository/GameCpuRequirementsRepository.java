package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.GameCpuRequirements;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameCpuRequirementsRepository extends JpaRepository<GameCpuRequirements, Long> {


    public  void deleteByGameId(Long gameId);
}
