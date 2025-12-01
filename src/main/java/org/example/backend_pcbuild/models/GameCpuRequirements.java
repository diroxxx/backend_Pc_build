package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;

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
    private Processor processor;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
}
