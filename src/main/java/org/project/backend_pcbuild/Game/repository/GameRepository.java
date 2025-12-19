package org.project.backend_pcbuild.Game.repository;

import org.project.backend_pcbuild.Game.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByTitle(String title);
}
