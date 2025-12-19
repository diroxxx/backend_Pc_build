package org.project.backend_pcbuild.Game.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.project.backend_pcbuild.pcComponents.model.Processor;
import org.project.backend_pcbuild.Game.dto.RecGameLevel;

@Data
@Entity
public class GameCpuRequirements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RecGameLevel recGameLevel;

    @ManyToOne
    @JoinColumn(name = "processor_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Processor processor;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Game game;
}
