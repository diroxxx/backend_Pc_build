package org.example.backend_pcbuild.repository;

import org.example.backend_pcbuild.models.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
