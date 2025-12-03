package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
public class GameGpuRequirements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RecGameLevel recGameLevel;

    @ManyToOne
    @JoinColumn(name = "gpu_model_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GpuModel gpuModel;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Game game;
}
